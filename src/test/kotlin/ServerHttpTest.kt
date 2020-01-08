import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.carousal.server.SERVER_ACCESS_HEADER
import com.carousal.server.Server

class ServerHttpTest {
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
    fun shouldAccessServerWithServerHeader() {
        val query = mapOf("query" to "nothing")
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(SERVER_ACCESS_HEADER, "PASSWORD")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
        }
    }

    @Test
    fun shouldFailServerWithServerHeader() {
        val query = mapOf("query" to "nothing")
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body).url("http://localhost:${server.port()}/graphql").build()
        client.newCall(request).execute().use { response ->
            Assertions.assertFalse(response.isSuccessful)
            assert(response.code == 403)
        }
    }

    @Test
    fun shouldSucceedIfNoServerPassword() {
        server.setServerPassword(null)
        val query = mapOf("query" to "nothing")
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
        }
    }
}
