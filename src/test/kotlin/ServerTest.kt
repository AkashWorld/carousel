import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import com.carousal.server.Server

class ServerTests {
    @Test
    fun initializationTest() {
        val server = Server.getInstance()
        assertDoesNotThrow { server.initialize() }
    }
}