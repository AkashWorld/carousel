import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import com.carousal.server.AUTH_HEADER
import com.carousal.server.Server

class GraphQLMediaSubscriptionTest {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val client: OkHttpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var server: Server

    @BeforeEach
    fun setUpServer() {
        server = Server.getInstance()
        server.initialize()
    }

    @AfterEach
    fun closeServer() {
        Server.clear()
    }

    private fun signInMutation(): String {
        val signInMutation = """
            mutation SignInMutation(${"$"}username: String!) {
                signIn(username: ${"$"}username)
            }
        """.trimIndent()
        val variables = mapOf("username" to "test")
        val query = mapOf("query" to signInMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            return (gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("data") as JsonObject).get("signIn").asString
        }
    }

    private fun mediaPlayMutation(currentTime: Double, token: String) {
        val mediaPlayMutation = """
            mutation MediaPlay {
                play
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to currentTime.toString())
        val query = mapOf("query" to mediaPlayMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(AUTH_HEADER, token)
            .build()
        client.newCall(request).execute()
    }

    private fun mediaSeekMutation(currentTime: Double, token: String) {
        val mediaPlayMutation = """
            mutation MediaSeek(${"$"}currentTime: Float!) {
                seek(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to currentTime.toString())
        val query = mapOf("query" to mediaPlayMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(AUTH_HEADER, token)
            .build()
        client.newCall(request).execute()
    }

    @Test
    fun graphqlSubscriptionTest() {
        var count = 0
        val token = signInMutation()
        val mediaSubscription = """
            subscription {
                mediaActions {
                    action
                    currentTime
                    user
                }
            }
        """.trimIndent()
        val variables = mapOf<String, Any>()
        val query = gson.toJson(mapOf("query" to mediaSubscription, "variables" to variables))
        val wsRequest: Request = Request.Builder().url("ws://localhost:${server.port()}/subscription").addHeader(
            AUTH_HEADER, token
        ).build()
        val wsListener: WebSocketListener = object : WebSocketListener() {
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WS Test Failure", t)
                assert(false)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.info(text)
                count += 1
            }
        }
        client.newWebSocket(wsRequest, wsListener).send(query)
        Thread.sleep(100)
        mediaPlayMutation(10.5, token)
        Thread.sleep(100)
        mediaSeekMutation(11.5, token)
        Thread.sleep(100)
        mediaPlayMutation(12.5, token)
        assert(count == 3)
    }
}
