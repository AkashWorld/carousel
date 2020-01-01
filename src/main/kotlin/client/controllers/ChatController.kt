package client.controllers

import client.models.ChatModel
import client.models.ClientContext
import client.models.Message
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ObservableBooleanValue
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import tornadofx.Controller
import java.time.Instant
import kotlin.collections.set
import kotlin.random.Random

class ChatController : Controller() {
    private val clientContext: ClientContext by param()
    private val chatModel = ChatModel(clientContext)
    private val isChatShown = SimpleBooleanProperty(true)
    private val colorMap = mutableMapOf<String, Color>()
    private val colorSet = mutableSetOf<Color>()

    fun getMessages(): ObservableList<Message> {
        return chatModel.getChatList()
    }

    fun addMessage(content: String) {
        chatModel.addMessage(content)
    }

    fun subscribeToMessages() {
        chatModel.subscribeToMessages()
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
                } else if (colonStart) {
                    currToken += c
                    retList.add(currToken)
                    currToken = ""
                    colonStart = false
                }
            } else if (c == ' ' && colonStart) {
                colonStart = false
                currToken += c
            } else {
                currToken += c
            }
        }
        if (currToken != "") {
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

    fun setChatShown(chatShown: Boolean) {
        isChatShown.value = chatShown
    }

    fun isChatShown(): ObservableBooleanValue {
        return isChatShown
    }
}

