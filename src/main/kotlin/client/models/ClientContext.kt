package client.models

import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import server.AUTH_HEADER
import server.SERVER_ACCESS_HEADER
import tornadofx.runLater
import java.io.IOException

/**
 * Represents the context for the client to connect to the server
 */
interface ClientContext {
    fun requestSignInToken(
        username: String,
        address: String,
        password: String?,
        success: () -> Unit,
        error: (String?) -> Unit
    )

    fun sendSignOutRequest()
    fun isValidContext(): Boolean
    fun getServerAddress(): String?
    fun getContextToken(): String?
    fun getUsername(): String?
    fun sendSubscriptionRequest(
        query: String,
        variables: Map<String, Any>?,
        responseHandler: (String) -> Unit,
        error: () -> Unit
    ): WebSocket?

    fun sendQueryOrMutationRequest(
        query: String,
        variables: Map<String, Any>?,
        success: (body: String) -> Unit,
        error: () -> Unit
    )

    fun clearContext()
}

class ClientContextImpl private constructor() : ClientContext {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val client = OkHttpClient()
    private var serverAddress: String? = null
    private var serverPassword: String? = null
    private var usernameTokenPair: Pair<String, String>? = null

    override fun requestSignInToken(
        username: String,
        address: String,
        password: String?,
        success: () -> Unit,
        error: (String?) -> Unit
    ) {
        val gson = Gson()
        val signInMutation = """
            mutation SignInMutation(${"$"}username: String!) {
                signIn(username: ${"$"}username)
            }
        """.trimIndent()
        val variables = mapOf("username" to username)
        val query = mapOf("query" to signInMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://${address}:57423/graphql")
            .header(SERVER_ACCESS_HEADER, password ?: "")
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                usernameTokenPair = null
                error(e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = gson.fromJson(response.body?.string(), Map::class.java) as Map<*, *>
                    usernameTokenPair = Pair(username, (body["data"] as Map<*, *>)["signIn"] as String)
                    serverAddress = address
                    serverPassword = password
                    success()
                } catch (e: Exception) {
                    usernameTokenPair = null
                    logger.error(e.message, e.cause)
                    error("Received error response from the server")
                }
            }
        })

    }

    override fun getUsername(): String? {
        return usernameTokenPair?.first
    }

    override fun isValidContext(): Boolean = usernameTokenPair != null

    override fun getServerAddress(): String? {
        return serverAddress
    }

    override fun sendSignOutRequest() {
        if (this.usernameTokenPair == null) {
            return
        }
        val query = """
            mutation SignOut {
                signOut
            }
        """.trimIndent()
        val gson = Gson()
        val queryMap = mapOf("query" to query, "variables" to null)
        val body: RequestBody = gson.toJson(queryMap).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://${serverAddress}:57423/graphql")
            .header(SERVER_ACCESS_HEADER, serverPassword ?: "")
            .header(AUTH_HEADER, usernameTokenPair!!.second)
            .build()
        client.newCall(request).execute()
    }


    override fun sendSubscriptionRequest(
        query: String,
        variables: Map<String, Any>?,
        responseHandler: (String) -> Unit,
        error: () -> Unit
    ): WebSocket? {
        if (serverAddress == null || usernameTokenPair == null) {
            logger.error("Must set the server address!")
            return null
        }
        val gson = Gson()
        val queryMap = gson.toJson(mapOf("query" to query, "variables" to variables))
        val wsRequestBuilder = Request.Builder().url("ws://${serverAddress}:57423/subscription").addHeader(
            AUTH_HEADER, usernameTokenPair!!.second
        )
        if (serverPassword != null) {
            wsRequestBuilder.addHeader(SERVER_ACCESS_HEADER, serverPassword!!)
        }
        val wsRequest: Request = wsRequestBuilder.build()
        val wsListener: WebSocketListener = object : WebSocketListener() {
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WebSocket Failure", t)
                error()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                logger.info(reason)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.info(text)
                try {
                    responseHandler(text)
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                    error()
                }
            }
        }
        val ws = client.newWebSocket(wsRequest, wsListener)
        ws.send(queryMap)
        return ws
    }

    override fun sendQueryOrMutationRequest(
        query: String,
        variables: Map<String, Any>?,
        success: (body: String) -> Unit,
        error: () -> Unit
    ) {
        if (serverAddress == null || usernameTokenPair == null) {
            logger.error("Must set the server address!")
            error()
            return
        }
        val gson = Gson()
        val queryMap = mapOf("query" to query, "variables" to variables)
        val body: RequestBody = gson.toJson(queryMap).toRequestBody()
        val requestBuilder = Request.Builder().post(body)
            .url("http://${serverAddress}:57423/graphql")
            .header(AUTH_HEADER, usernameTokenPair!!.second)
        if (serverPassword != null) {
            requestBuilder.addHeader(SERVER_ACCESS_HEADER, serverPassword!!)
        }
        val request = requestBuilder.build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                error()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.body == null) {
                        logger.error("Response body is null")
                        error()
                        return
                    } else {
                        success(response.body!!.string())
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                    error()
                }
            }
        })
    }

    override fun getContextToken(): String? {
        return usernameTokenPair?.second
    }

    override fun clearContext() {
        serverAddress = null
        serverPassword = null
        usernameTokenPair = null
    }

    companion object {
        private var context: ClientContext? = null

        fun getInstance(): ClientContext {
            if (context == null) {
                context = ClientContextImpl()
            }
            return context as ClientContext
        }
    }
}