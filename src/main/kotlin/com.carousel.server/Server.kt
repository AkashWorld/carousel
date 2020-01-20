package com.carousel.server

import com.carousel.server.model.ChatRepository
import com.carousel.server.model.UserAuthenticationImpl
import com.carousel.server.model.UsersRepository
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.LoggerFactory
import io.javalin.http.ForbiddenResponse
import io.javalin.websocket.WsMessageContext
import java.util.concurrent.CompletableFuture
import com.carousel.server.GraphQLProvider as GraphQLProvider

const val DEFAULT_PORT = 57423
const val SERVER_ACCESS_HEADER = "ServerAuth"
const val AUTH_HEADER = "Authorization"


class Server private constructor() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val uPnPProvider: UPnPProvider = UPnPProviderImpl(port = DEFAULT_PORT)
    private var server: Javalin = Javalin.create()
    private var graphQLProvider: GraphQLProvider? = null
    private val usersRepository = UsersRepository()
    private val userAuthentication = UserAuthenticationImpl(usersRepository)
    private val serverAuthentication = ServerAuthentication()
    private val externalIPProvider: ExternalIPProvider = ExternalIPProviderImpl()

    fun initialize(port: Int = DEFAULT_PORT) {
        try {
            this.server = Javalin.create()
            /**
             * 5.5MB cache for 5MB image limit
             */
            this.server.config.requestCacheSize = 5767168L
            this.server.before("*") {
                val serverAccessHeader = it.header(SERVER_ACCESS_HEADER)
                this.serverAccess(serverAccessHeader)
            }
            this.server.post("/graphql") {
                this.serveGraphQLRequest(it)
            }
            this.server.wsBefore { handler ->
                handler.onConnect {
                    val serverAccessHeader = it.header(SERVER_ACCESS_HEADER)
                    this.serverAccess(serverAccessHeader)
                }
            }
            this.server.ws("/subscription") { handler ->
                handler.onConnect {
                    it.session.idleTimeout = 7200000L //2 hours timeout
                    val token = it.header(AUTH_HEADER)
                    val user = userAuthentication.verifyUser(token)
                    if (user == null) {
                        logger.error("WS Access denied, user token not found")
                        throw ForbiddenResponse("There isn't any popcorn here!")
                    }
                    logger.info("${user.username} connected via WS")
                }
                handler.onMessage {
                    /**
                     * Message maybe empty due to heartbeats sent to upkeep connection
                     */
                    if (it.message().isNotEmpty()) {
                        serveSubscriptionGraphQLRequest(it)
                    } else {
                        logger.info("WS Heartbeat")
                    }
                }
                handler.onError {
                    val token = it.header(AUTH_HEADER)
                    val user = userAuthentication.verifyUser(token)
                    user?.run {
                        usersRepository.removeUser(this)
                        logger.error("WS error by ${this.username}", it.error())
                    }
                }
                handler.onClose {
                    val token = it.header(AUTH_HEADER)
                    val user = userAuthentication.verifyUser(token)
                    user?.run {
                        usersRepository.removeUser(this)
                        logger.info("WS closed by ${this.username}", it.reason())
                    }
                }
            }
            server.start(port)
        } catch (e: Exception) {
            close()
            logger.error(e.message)
            throw(e)
        } finally {
            val chatFeedRepository = ChatRepository()
            graphQLProvider = GraphQLProvider(usersRepository, chatFeedRepository, userAuthentication)
        }
    }

    fun initPortForwarding(): Boolean {
        return try {
            uPnPProvider.requestMapping()
        } catch (e: Exception) {
            close()
            logger.error(e.message)
            false
        }
    }

    fun getExternalIP(): String {
        val upnpExternalIp = uPnPProvider.tryGetExternalIp()
        if (upnpExternalIp != null) {
            return upnpExternalIp
        }
        val future = CompletableFuture<String>()
        externalIPProvider.getExternalIp(future)
        try {
            return future.get()
        } catch (e: Exception) {
            close()
            throw Exception(e.message)
        }
    }

    fun close() {
        try {
            server.stop()
        } catch (e: Exception) {
            logger.error("Could not stop server - ${e.message}")
        } finally {
            uPnPProvider.release()
            usersRepository.clear()
            serverAuthentication.setServerPassword(null)
            graphQLProvider = null
        }
    }

    fun setServerPassword(password: String?) {
        serverAuthentication.setServerPassword(password)
    }

    fun port(): Int {
        return this.server.port()
    }

    private fun serveGraphQLRequest(context: Context) {
        logger.info("post /graphql from ip:${context.ip()}")
        logger.info("Request query: ${context.body()}")
        val user = userAuthentication.verifyUser(context.header(AUTH_HEADER))
        val result = this.graphQLProvider?.serveGraphQLQueryMutation(context.body(), user)
        if (result != null) {
            context.status(200).json(result)
            return
        }
        context.status(400).result("Error handling GraphQL request")
    }

    private fun serveSubscriptionGraphQLRequest(handler: WsMessageContext) {
        val token = handler.header(AUTH_HEADER)
        val user = userAuthentication.verifyUser(token)
        graphQLProvider?.serveGraphQLSubscription(handler, user)
    }

    private fun serverAccess(serverAccessHeader: String?) {
        val result = this.serverAuthentication.verifyPassword(serverAccessHeader)
        if (!result) {
            logger.error("Server access denied - ServerAuth: $serverAccessHeader")
            throw ForbiddenResponse("There isn't any popcorn here!")
        }
    }

    /**
     * Lazy init singleton com.carousel.server
     */
    companion object {
        private var server: Server? = null

        fun getInstance(): Server {
            if (server == null) {
                server = Server()
            }
            return server as Server
        }

        fun clear() {
            server?.close()
        }
    }
}
