package server

import graphql.ExecutionInput
import io.javalin.Javalin
import io.javalin.http.Context
import org.slf4j.LoggerFactory
import com.google.gson.Gson
import io.javalin.http.ForbiddenResponse
import io.javalin.websocket.WsContext
import io.javalin.websocket.WsMessageContext
import server.model.*
import server.GraphQLProvider as GraphQLProvider

const val DEFAULT_PORT = 57423
const val SERVER_ACCESS_HEADER = "ServerAuth"
const val AUTH_HEADER = "Authorization"


class Server constructor(private val port: Int = DEFAULT_PORT) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val server: Javalin = Javalin.create()
    private val usersRepository = UsersRepository()
    private val chatFeedRepository = ChatFeedRepository()
    private val userAuthentication = UserAuthenticationImpl(usersRepository)
    private var graphQLProvider: GraphQLProvider = GraphQLProvider(usersRepository, chatFeedRepository, userAuthentication)
    private val serverAuthentication = ServerAuthentication()

    init {
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
                val token = it.header(AUTH_HEADER)
                val user = userAuthentication.verifyUser(token)
                if (user == null) {
                    logger.error("WS Access denied, user token not found")
                    throw ForbiddenResponse("There isn't any popcorn here!")
                }
                logger.info("${user.getUsername()} connected via WS")
            }
            handler.onMessage {
                serveSubscriptionGraphQLRequest(it)
            }
            handler.onError {
                logger.error("WS error", it.error())
            }
            handler.onClose {
                logger.info("WS closed", it.reason())
            }
        }
    }

    fun initialize(): Boolean {
        try {
            this.server.start(port)
        } catch (e: Exception) {
            logger.error(e.message)
            return false
        }
        return true
    }

    fun close() {
        server.stop()
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
        val result = this.graphQLProvider.serveGraphQLQueryMutation(context.body(), user)
        if (result != null) {
            context.status(200).json(result)
            return
        }
        context.status(400).result("Error handling GraphQL request")
    }

    private fun serveSubscriptionGraphQLRequest(handler: WsMessageContext) {
        val token = handler.header(AUTH_HEADER)
        val user = userAuthentication.verifyUser(token)
        graphQLProvider.serveGraphQLSubscription(handler, user)
    }

    private fun serverAccess(serverAccessHeader: String?) {
        val result = this.serverAuthentication.verifyPassword(serverAccessHeader)
        if (!result) {
            logger.error("Server access denied - ServerAuth: $serverAccessHeader")
            throw ForbiddenResponse("There isn't any popcorn here!")
        }
    }
}
