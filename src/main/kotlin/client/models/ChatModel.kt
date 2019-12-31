package client.models

import com.google.gson.Gson
import javafx.collections.ObservableList
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import server.AUTH_HEADER
import tornadofx.runLater
import tornadofx.toObservable
import java.io.IOException

enum class ContentType {
    IMAGE,
    MESSAGE,
    INFO,
    NONE
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
        if (clientContext.getContextToken() == null) {
            logger.error("Client context not found")
            return
        }
        val gson = Gson()
        val mutation = """
            mutation InsertMutation(${"$"}message: String!){
                insertMessage(message: ${"$"}message)
            }
        """.trimIndent()
        val variables = mapOf("message" to content)
        val query = mapOf("query" to mutation, "variables" to variables)
        val body: RequestBody = gson.toJson(query).toRequestBody()
        val request = Request.Builder().post(body)
            .url("http://${clientContext.getServerAddress()}/graphql")
            .header(AUTH_HEADER, clientContext.getContextToken()!!)
            .build()
        clientContext.client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                logger.error(e.message, e.cause)
            }

            override fun onResponse(call: Call, response: Response) {
                logger.info(response.body.toString())
            }
        })
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
        val query = gson.toJson(mapOf("query" to subscription, "variables" to variables))
        val wsRequest: Request =
            Request.Builder().url("ws://${clientContext.getServerAddress()}/subscription").addHeader(
                AUTH_HEADER, clientContext.getContextToken()!!
            ).build()
        val wsListener: WebSocketListener = object : WebSocketListener() {
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                logger.error("WebSocket Failure", t)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.info(text)
                try {
                    val result: Map<String, String> =
                        gson.fromJson(text, Map::class.java)["chatFeed"] as Map<String, String>
                    val username = result["username"]
                    val content = result["content"]
                    if (username != null && content != null) {
                        runLater {
                            chatList.add(Message(username, content))
                        }
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                }
            }
        }
        clientContext.client.newWebSocket(wsRequest, wsListener).send(query)
    }
}