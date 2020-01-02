import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import server.Server

class ServerTests {
    @Test
    fun initializationTest() {
        val server = Server.getInstance()

        assertTrue(server.initialize()) { "Should initialize server" }
    }
}