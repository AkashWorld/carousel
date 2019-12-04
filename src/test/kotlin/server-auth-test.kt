import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import server.ServerAuthentication

class ServerAuthTest {
    @Test
    fun successServerAuthPassword() {
        val serverAuthentication = ServerAuthentication()
        val password = "SAMPLE_PASSWORD"
        serverAuthentication.setServerPassword(password)
        val result = serverAuthentication.verifyPassword(password)
        assertTrue(result)
    }

    @Test
    fun failServerAuthPassword() {
        val serverAuthentication = ServerAuthentication()
        serverAuthentication.setServerPassword("SAMPLE_PASSWORD")
        val result = serverAuthentication.verifyPassword("NOT_PASSWORD")
        assertFalse(result)
    }

    @Test
    fun allowSuccessForNullPassword() {
        val serverAuthentication = ServerAuthentication()
        assertTrue(serverAuthentication.verifyPassword(null))
        assertTrue(serverAuthentication.verifyPassword("NOT_PASSWORD"))
    }
}