package client.models

import com.google.gson.Gson
import javafx.collections.ObservableList
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
private data class ChatFeedSubscriptionResponse(val chatFeed: Message)

class ChatModel {
    private val clientContext: ClientContext = ClientContextImpl.getInstance()
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val chatList: ObservableList<Message> = mutableListOf<Message>().toObservable()
    private val allMessages: MutableList<Message> = mutableListOf()
    private var isInfoMessageShown = true

    fun sendInsertMessageRequest(content: String) {
        val mutation = """
            mutation InsertMutation(${"$"}message: String!){
                insertMessage(message: ${"$"}message)
            }
        """.trimIndent()
        val variables = mapOf("message" to content)
        clientContext.sendQueryOrMutationRequest(mutation, variables, {}, {})
    }

    fun sendInsertImageRequest(encodedImage: String, error: () -> Unit) {
        val mutation = """
           mutation InsertImage(${"$"}data: String!){
                insertImage(data: ${"$"}data)
           }
       """.trimIndent()
        val variables = mapOf("data" to encodedImage)
        clientContext.sendQueryOrMutationRequest(mutation, variables, {}, error)
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
                Gson().fromJson(it, GraphQLResponse::class.java).data.getMessagesPaginated.forEach { addMessage(it) }
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
        val subscription = """
            subscription {
                chatFeed{
                    username
                    content
                    contentType
                }
            }
        """.trimIndent()
        val variables = mapOf<String, Any>()
        clientContext.sendSubscriptionRequest(subscription, variables, { body ->
            try {
                val response = Gson().fromJson(body, ChatFeedSubscriptionResponse::class.java)
                response?.chatFeed?.run {
                    runLater {
                        addMessage(this)
                    }
                }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
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

    fun clear() {
        allMessages.clear()
        chatList.clear()
    }
}