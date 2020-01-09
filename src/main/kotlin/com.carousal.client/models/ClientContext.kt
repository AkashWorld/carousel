package com.carousal.client.models

import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Represents the context for the com.carousal.client to connect to the com.carousal.server
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
    )

    fun sendQueryOrMutationRequest(
        query: String,
        variables: Map<String, Any>?,
        success: (body: String) -> Unit,
        error: () -> Unit
    )

    fun clearContext()

    companion object {
        const val SERVER_ACCESS_HEADER = "ServerAuth"
        const val AUTH_HEADER = "Authorization"
        const val DEFAULT_PORT = "57423"
    }
}

class ClientContextImpl private constructor() : ClientContext {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val client = OkHttpClient()
    private var serverAddress: String? = null
    private var serverPassword: String? = null
    private var usernameTokenPair: Pair<String, String>? = null
    private var wsListener: WSListener? = null
    private var webSocket: WebSocket? = null

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
            .url("http://${address}/graphql")
            .header(ClientContext.SERVER_ACCESS_HEADER, password ?: "")
            .build()
        try {
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
                        setUpWebSocketConnection()
                        success()
                    } catch (e: Exception) {
                        usernameTokenPair = null
                        logger.error(e.message, e.cause)
                        error("Received error response from the server")
                    }
                }
            })
        } catch(e: Exception) {
            logger.error(e.message, e.cause)
            error("Could not connect to server.")
        }
    }

    private fun setUpWebSocketConnection() {
        val wsRequestBuilder = Request.Builder().url("ws://${serverAddress}/subscription").addHeader(
            ClientContext.AUTH_HEADER, usernameTokenPair!!.second
        )
        if (serverPassword != null) {
            wsRequestBuilder.addHeader(ClientContext.SERVER_ACCESS_HEADER, serverPassword!!)
        }
        val wsRequest: Request = wsRequestBuilder.build()
        wsListener = WSListener()
        webSocket = client.newWebSocket(wsRequest, wsListener!!)
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
            .url("http://${serverAddress}/graphql")
            .header(ClientContext.SERVER_ACCESS_HEADER, serverPassword ?: "")
            .header(ClientContext.AUTH_HEADER, usernameTokenPair!!.second)
            .build()
        val call = client.newCall(request)
        call.timeout().deadline(2, TimeUnit.SECONDS)
        call.execute()
    }


    override fun sendSubscriptionRequest(
        query: String,
        variables: Map<String, Any>?,
        responseHandler: (String) -> Unit,
        error: () -> Unit
    ) {
        if (serverAddress == null || usernameTokenPair == null || webSocket == null || wsListener == null) {
            logger.error("ClientContext websocket is not initialized")
            error()
            return
        }
        if (wsListener?.addWebSocketHandler(query, responseHandler, error) == true) {
            webSocket?.send(Gson().toJson(mapOf("query" to query, "variables" to variables)))
        }
    }

    override fun sendQueryOrMutationRequest(
        query: String,
        variables: Map<String, Any>?,
        success: (body: String) -> Unit,
        error: () -> Unit
    ) {
        if (serverAddress == null || usernameTokenPair == null) {
            logger.error("Must set the com.carousal.server address!")
            error()
            return
        }
        val gson = Gson()
        val queryMap = mapOf("query" to query, "variables" to variables)
        val body: RequestBody = gson.toJson(queryMap).toRequestBody()
        val requestBuilder = Request.Builder().post(body)
            .url("http://${serverAddress}/graphql")
            .header(ClientContext.AUTH_HEADER, usernameTokenPair!!.second)
        if (serverPassword != null) {
            requestBuilder.addHeader(ClientContext.SERVER_ACCESS_HEADER, serverPassword!!)
        }
        val request = requestBuilder.build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                error()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    if (response.body == null || response.code != 200) {
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
        webSocket?.close(1001, "Clearing Context")
        webSocket = null
        wsListener = null
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