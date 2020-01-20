import com.carousel.server.*
import okhttp3.OkHttpClient
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture

class ExternalIPTest {
    private val client = OkHttpClient()
    @Test
    fun checkIpRetrievers() {
        var future = CompletableFuture<String>()
        AmazonAWS(client).retrieveIP(future)
        var address = future.get()
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(address))
        future = CompletableFuture()
        ICanHazIp(client).retrieveIP(future)
        address = future.get()
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(address))
        future = CompletableFuture()
        MyExternalIP(client).retrieveIP(future)
        address = future.get()
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(address))
        future = CompletableFuture()
        IPInfo(client).retrieveIP(future)
        address = future.get()
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(address))
    }

    @Test
    fun shouldReturnExternalIp() {
        val provider = ExternalIPProviderImpl()
        val future = CompletableFuture<String>()
        provider.getExternalIp(future)
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(future.get()))
        val newFuture = CompletableFuture<String>()
        provider.getExternalIp(newFuture)
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure(newFuture.get()))
    }

    @Test
    fun checkIpStructure() {
        assert(ExternalIPProviderImpl.validateIPV4AddressStructure("172.100.33.23"))
        assert(!ExternalIPProviderImpl.validateIPV4AddressStructure("256.100.33.23"))
        assert(!ExternalIPProviderImpl.validateIPV4AddressStructure("256.100"))
    }

    @Test
    fun testPortMapper() {
        val upnp = UPnPProviderImpl(42700)
        upnp.requestMapping()
    }
}