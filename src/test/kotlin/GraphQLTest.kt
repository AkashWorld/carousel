import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.carousel.server.Server

class GraphQLTest {
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

    @Test
    fun graphqlEndpointSuccessful() {
        val query = mapOf("query" to "nothing")
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body).url("http://localhost:${server.port()}/graphql").build()
        client.newCall(request).execute().use { response ->
            assertTrue(response.isSuccessful)
            assert(response.code == 200)
        }
    }

}
