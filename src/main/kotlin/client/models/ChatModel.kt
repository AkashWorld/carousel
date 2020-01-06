package client.models

import com.google.gson.Gson
import com.google.gson.JsonObject
import javafx.collections.ObservableList
import okhttp3.WebSocket
import org.slf4j.LoggerFactory
import tornadofx.runLater
import tornadofx.toObservable

enum class ContentType {
    IMAGE,
    MESSAGE,
    INFO,
}

data class Message(
    val username: String,
    val content: String,
    val contentType: ContentType = ContentType.MESSAGE
)

private data class GetMessagesPaginatedObject(val getMessagesPaginated: List<Message>)
private data class GraphQLResponse(val data: GetMessagesPaginatedObject)

class ChatModel {
    private val clientContext: ClientContext = ClientContextImpl.getInstance()
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val chatList: ObservableList<Message> = mutableListOf<Message>().toObservable()
    private val allMessages: MutableList<Message> = mutableListOf()
    private var isInfoMessageShown = true
    private var ws: WebSocket? = null

    fun sendInsertMessageRequest(content: String) {
        val mutation = """
            mutation InsertMutation(${"$"}message: String!){
                insertMessage(message: ${"$"}message)
            }
        """.trimIndent()
        val variables = mapOf("message" to content)
        clientContext.sendQueryOrMutationRequest(mutation, variables, {}, {})
    }

    fun sendGetPaginatedMessagesRequest(count: Int = 50, error: () -> Unit) {
        val mutation = """
            query GetPaginatedMessages(${"$"}start: Int!, ${"$"}count: Int!){
                getMessagesPaginated(start: ${"$"}start, count: ${"$"}count){
                    username
                    content
                    contentType
                }
            }
        """.trimIndent()
        val variables = mapOf("start" to 0, "count" to count)
        clientContext.sendQueryOrMutationRequest(mutation, variables, {
            try {
                val gson = Gson()
                gson.fromJson(it, GraphQLResponse::class.java).data.getMessagesPaginated.forEach { addMessage(it) }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                error()
            }
        }, error)
    }

    fun getChatList(): ObservableList<Message> {
        return this.chatList
    }

    fun addMessage(message: Message) {
        allMessages.add(message)
        if (!isInfoMessageShown && message.contentType == ContentType.INFO) {
            return
        }
        runLater {
            chatList.add(message)
        }
    }

    fun subscribeToMessages(error: () -> Unit) {
        val gson = Gson()
        val subscription = """
            subscription {
                chatFeed{
                    username
                    content
                }
            }
        """.trimIndent()
        val variables = mapOf<String, Any>()
        ws = clientContext.sendSubscriptionRequest(subscription, variables, { body ->
            try {
                val result: Map<String, String> =
                    gson.fromJson(body, Map::class.java)["chatFeed"] as Map<String, String>
                val username = result["username"]
                val content = result["content"]
                if (username != null && content != null) {
                    runLater {
                        addMessage(Message(username, content))
                    }
                }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                error()
            }
        }, error)
    }

    fun toggleIsInfoShown() {
        isInfoMessageShown = !isInfoMessageShown
        chatList.clear()
        if (isInfoMessageShown) {
            chatList.addAll(allMessages)
        } else {
            chatList.addAll(allMessages.filter { it.contentType != ContentType.INFO })
        }
    }

    fun releaseSubscription() {
        ws?.run { this.close(1001, "Undock") }
    }

    fun clear() {
        allMessages.clear()
        chatList.clear()
        releaseSubscription()
        ws = null
    }
}