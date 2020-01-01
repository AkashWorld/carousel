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
    val user: String,
    val content: String,
    val contentType: ContentType = ContentType.MESSAGE
)


class ChatModel(private val clientContext: ClientContext) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val chatList: ObservableList<Message> = mutableListOf<Message>().toObservable()

    fun addMessage(content: String) {
        val mutation = """
            mutation InsertMutation(${"$"}message: String!){
                insertMessage(message: ${"$"}message)
            }
        """.trimIndent()
        val variables = mapOf("message" to content)
        clientContext.sendQueryOrMutationRequest(mutation, variables, {}, {})
    }

    fun getChatList(): ObservableList<Message> {
        return this.chatList
    }

    fun subscribeToMessages() {
        if (clientContext.getContextToken() == null) {
            logger.error("Client context not found")
            return
        }
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
        clientContext.sendSubscriptionRequest(subscription, variables, { body ->
            val result: Map<String, String> =
                gson.fromJson(body, Map::class.java)["chatFeed"] as Map<String, String>
            val username = result["username"]
            val content = result["content"]
            if (username != null && content != null) {
                runLater {
                    chatList.add(Message(username, content))
                }
            }
        }, {})
    }
}