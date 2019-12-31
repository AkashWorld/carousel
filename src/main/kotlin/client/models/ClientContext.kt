package client.models

import com.google.gson.Gson
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import java.io.IOException

class ClientContext(private val address: String) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var usernameTokenPair: Pair<String, String>? = null
    val client = OkHttpClient()

    fun requestSignInToken(username: String, success: () -> Unit, error: () -> Unit) {
        val gson = Gson()
        val signInMutation = """
            mutation SignInMutation(${"$"}username: String!) {
                signIn(username: ${"$"}username)
            }
        """.trimIndent()
        /**
         * TODO Change token username
         */
        val variables = mapOf("username" to username)
        val query = mapOf("query" to signInMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://${address}/graphql")
            .build()
        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
                error()
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val body = gson.fromJson(response.body?.string(), Map::class.java) as Map<*, *>
                    usernameTokenPair = Pair(username, (body["data"] as Map<*, *>)["signIn"] as String)
                } catch(e: Exception) {
                    logger.error(e.message, e.cause)
                    error()
                }
                success()
            }
        })

    }

    fun getServerAddress(): String {
        return address
    }

    fun getContextToken(): String? {
        return usernameTokenPair?.second
    }
}