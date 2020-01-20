import com.carousel.client.models.ChatModel
import com.carousel.client.models.ClientContextImpl
import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import com.carousel.server.Server
import com.carousel.server.model.ChatRepository
import com.carousel.server.model.ContentType
import com.carousel.server.model.User
import java.util.concurrent.CompletableFuture


data class MessageResponse(val username: String, val content: String, val contentType: ContentType)
data class GetMessagesPaginatedObject(val getMessagesPaginated: List<MessageResponse>)
data class GraphQLResponse(val data: GetMessagesPaginatedObject)
class PaginatedMessagesTest {
    @Test
    fun shouldReturnCorrectAmountInList() {
        val chatFeed = ChatRepository()
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        assert(chatFeed.getPaginatedMessages(0, 8).size == 8)
    }

    @Test
    fun shouldReturnOneWithBadPaginated() {
        val chatFeed = ChatRepository()
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        assert(chatFeed.getPaginatedMessages(0, 50).size == 1)
    }

    @Test
    fun shouldThrowOnOutOfRangeIndexes() {
        val chatFeed = ChatRepository()
        chatFeed.addMessage(User("Test", true, null), "Hello, World", ContentType.MESSAGE)
        assertThrows<Exception> { chatFeed.getPaginatedMessages(2, 5) }
    }

    @Test
    fun shouldReturnEmpty() {
        val chatFeed = ChatRepository()
        assert(chatFeed.getPaginatedMessages(0, 50).isEmpty())
    }

    @Test
    fun shouldReturnFirstTwo() {
        val chatFeed = ChatRepository()
        chatFeed.addMessage(User("Test", true, null), "1", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "2", ContentType.MESSAGE)
        chatFeed.addMessage(User("Test", true, null), "3", ContentType.MESSAGE)

        val result = chatFeed.getPaginatedMessages(1, 2)
        assert(result.size == 2)
        assert(result[0].content == "1")
        assert(result[1].content == "2")
    }

    @Test
    fun graphqlMessageTest() {
        Server.getInstance().initialize()
        ClientContextImpl.getInstance().requestSignInToken("test", "localhost", "", {}, {})
        Thread.sleep(1000)
        val chatModel = ChatModel()
        chatModel.sendInsertMessageRequest("Hello, World")
        chatModel.sendInsertMessageRequest("Hello, World")
        chatModel.sendInsertMessageRequest("Hello, World")
        chatModel.sendInsertMessageRequest("Hello, World")
        chatModel.sendInsertMessageRequest("Hello, World")
        Thread.sleep(1000)
        val mutation = """
            query GetPaginatedMessages(${"$"}start: Int!, ${"$"}count: Int!){
                getMessagesPaginated(start: ${"$"}start, count: ${"$"}count){
                    username
                    content
                    contentType
                }
            }
        """.trimIndent()
        val variables = mapOf("start" to 0, "count" to 50)
        val future = CompletableFuture<GraphQLResponse>()
        ClientContextImpl.getInstance().sendQueryOrMutationRequest(mutation, variables, {
            val gson = Gson()
            val result = gson.fromJson(it, GraphQLResponse::class.java)
            assert(result.data.getMessagesPaginated.size == 5)
            future.complete(result)
        }, {assert(false)})
        future.get()
    }
}