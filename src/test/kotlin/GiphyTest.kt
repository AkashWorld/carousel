import com.carousel.server.model.Giphy
import org.junit.jupiter.api.Test

/**
 * For tests here to work, set the environment variable GIPHY_API_KEY
 */
class GiphyTest {
    @Test
    fun shouldReturnGiphyRandomId() {
        val client = Giphy()
        assert(client.getGIPHYRandomId()?.length!! > 0)
    }

    @Test
    fun shouldReturnGiphyQuery() {
        val client = Giphy()
        assert(client.getGiphySearchRequest("superman", null) != null)
    }

    @Test
    fun shouldReturnTrendingGiphyQuery() {
        val client = Giphy()
        assert(client.getGiphyTrendingRequest(null) != null)
    }
}