import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import server.Server

class ServerTests {
    @Test
    fun initializationTest() {
        val server = Server()

        assertTrue(server.initialize()) { "Should initialize server" }
    }

    @Test
    fun initializationTestWithDifferentPort() {
        val server = Server(57890)

        assertTrue(server.initialize()) { "Should initialize server" }
    }

    @Test
    fun initializeWithInvalidPort() {
        val server = Server(-1)

        assertFalse(server.initialize()) { "Should fail initialization" }
    }
}