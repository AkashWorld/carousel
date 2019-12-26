package client.controllers

import client.models.ChatModel
import client.models.ClientContext
import client.models.ContentType
import client.models.Message
import com.google.gson.Gson
import tornadofx.*
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.slf4j.LoggerFactory
import server.AUTH_HEADER
import java.io.IOException
import java.time.Instant
import kotlin.random.Random

class ChatController : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val clientContext: ClientContext by param()
    private val chatModel = ChatModel()
    private val colorMap = mutableMapOf<String, Color>()
    private val colorSet = mutableSetOf<Color>()

    init {
        for (i in 0..50) {
            chatModel.addMessage(Message("", "", contentType = ContentType.NONE))
        }
    }

    fun getMessages(): ObservableList<Message> {
        return chatModel.getChatList()
    }

    fun addMessage(content: String): Boolean {
        if (clientContext.getContextToken() == null) {
            logger.error("Client context not found")
            return false
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
        return true
    }

    fun subscribeToMessages(): Boolean {
        if (clientContext.getContextToken() == null) {
            logger.error("Client context not found")
            return false
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
                logger.error("WS Test Failure", t)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                logger.info(text)
                try {
                    val result: Map<String, String> =
                        gson.fromJson(text, Map::class.java)["chatFeed"] as Map<String, String>
                    val username = result["username"]
                    val content = result["content"]
                    if (username != null && content != null) {
                        chatModel.addMessage(Message(username, content))
                    }
                } catch (e: Exception) {
                    logger.error(e.message, e.cause)
                }
            }
        }
        clientContext.client.newWebSocket(wsRequest, wsListener).send(query)
        return true
    }

    fun tokenizeMessage(message: String): List<String> {
        val retList = mutableListOf<String>()
        var currToken = ""
        var colonStart = false
        for (element in message) {
            val c = element
            if (c == ':') {
                if (currToken != "" && !colonStart) {
                    retList.add(currToken)
                    currToken = ""
                }
                if (!colonStart) {
                    currToken += c
                    colonStart = true
                } else if(colonStart) {
                    currToken += c
                    retList.add(currToken)
                    currToken = ""
                    colonStart = false
                }
            } else if(c == ' ' && colonStart) {
                colonStart = false
                currToken += c
            } else {
                currToken += c
            }
        }
        if(currToken != "") {
            retList.add(currToken)
        }
        return retList
    }

    fun getColor(username: String): Color {
        if (colorMap.containsKey(username)) {
            return colorMap[username]!!
        }
        val rand = Random(Instant.now().nano)
        val baseColor = 32.0
        val maxColor = 232.0
        var newColor: Color? = null
        while (newColor == null || colorSet.contains(newColor)) {
            var colorList = mutableListOf(0.0, 0.0, 0.0)
            val low = rand.nextInt(0, 2)
            var high: Int? = null
            while (high == null || high == low) {
                high = rand.nextInt(0, 2)
            }
            colorList[low] = baseColor / 255.0
            colorList[high] = maxColor / 255.0
            colorList = colorList.map {
                if (it == 0.0) {
                    rand.nextDouble(0.0, 255.0) / 255.0
                } else {
                    it
                }
            }.toMutableList()
            newColor = Color(colorList[0], colorList[1], colorList[2], 1.0)
        }
        colorMap[username] = newColor
        colorSet.add(newColor)
        return newColor
    }
}

