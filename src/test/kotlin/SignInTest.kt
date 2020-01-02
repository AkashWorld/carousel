import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.SERVER_ACCESS_HEADER
import server.Server

class SignInTest {
    private val client: OkHttpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var server: Server

    @BeforeEach
    fun setUpServer() {
        server = Server.getInstance()
        server.setServerPassword("PASSWORD")
        server.initialize()
    }

    @AfterEach
    fun closeServer() {
        Server.clear()
    }

    @Test
    fun successResponseFromSignInMutation() {
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
            .header(SERVER_ACCESS_HEADER, "PASSWORD")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val key = (gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("data") as JsonObject).get("signIn").asString
            assert(key == "test")
        }
    }
}