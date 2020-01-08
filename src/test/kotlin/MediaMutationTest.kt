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
import com.carousal.server.AUTH_HEADER
import com.carousal.server.Server

class MediaMutationTest {
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

    @Test
    fun shouldFailMediaPlayMutationWithoutContext() {
        val mediaPlayMutation = """
            mutation MediaPlay(${"$"}currentTime: Float!) {
                play(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to "10.5")
        val query = mapOf("query" to mediaPlayMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("errors")
            assert(!result.isJsonNull)
        }
    }

    @Test
    fun shouldFailMediaPauseMutationWithoutContext() {
        val mediaPauseMutation = """
            mutation MediaPause(${"$"}currentTime: Float!) {
                pause(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to "10.5")
        val query = mapOf("query" to mediaPauseMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("errors")
            assert(!result.isJsonNull)
        }
    }

    @Test
    fun shouldFailMediaLoadMutationWithoutContext() {
        val mediaLoadMutation = """
            mutation MediaLoad(${"$"}file: String!) {
                load(file:${"$"}file)
            }
        """.trimIndent()
        val variables = mapOf("file" to "random-file.mkv")
        val query = mapOf("query" to mediaLoadMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("errors")
            assert(!result.isJsonNull)
        }
    }

    @Test
    fun shouldSucceedMediaPlayMutation() {
        val token = signInMutation()
        val mediaPlayMutation = """
            mutation MediaPlay(${"$"}currentTime: Float!) {
                play(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to "10.5")
        val query = mapOf("query" to mediaPlayMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(AUTH_HEADER, token)
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("data")
            assert(!result.isJsonNull)
        }
    }

    @Test
    fun shouldSucceedMediaPauseMutation() {
        val token = signInMutation()
        val mediaPauseMutation = """
            mutation MediaPause(${"$"}currentTime: Float!) {
                pause(currentTime:${"$"}currentTime)
            }
        """.trimIndent()
        val variables = mapOf("currentTime" to "10.5")
        val query = mapOf("query" to mediaPauseMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(AUTH_HEADER, token)
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("data")
            assert(!result.isJsonNull)
        }
    }

    @Test
    fun shouldSucceedMediaLoadMutation() {
        val token = signInMutation()
        val mediaLoadMutation = """
            mutation MediaLoad(${"$"}file: String!) {
                load(file:${"$"}file) {
                    id
                }
            }
        """.trimIndent()
        val variables = mapOf("file" to "random-file.mkv")
        val query = mapOf("query" to mediaLoadMutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://localhost:${server.port()}/graphql")
            .header(AUTH_HEADER, token)
            .build()
        client.newCall(request).execute().use { response ->
            Assertions.assertTrue(response.isSuccessful)
            assert(response.code == 200)
            assert(response.body != null)
            val result = gson.fromJson(response.body?.string(), JsonObject::class.java)
                .get("data")
            assert(!result.isJsonNull)
        }
    }
}