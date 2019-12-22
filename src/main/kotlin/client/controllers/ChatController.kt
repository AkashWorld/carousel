package client.controllers

import client.models.ChatModel
import client.models.ContentType
import client.models.Message
import tornadofx.*
import javafx.collections.ObservableList
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.random.Random

class ChatController(private val chatModel: ChatModel) : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val colorMap = mutableMapOf<String, Color>()
    private val colorSet = mutableSetOf<Color>()

    fun padMessages(height: Double) {
        val count = height / 27.0
        if (count <= chatModel.getChatList().size) {
            return
        }
        val needed = count - chatModel.getChatList().size + 1.0
        for (i in 0..needed.toInt()) {
            chatModel.getChatList().add(0, Message("", "", ContentType.MESSAGE))
        }
    }

    fun getMessages(): ObservableList<Message> {
        return chatModel.getChatList()
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

    fun clearColors() {
        colorMap.clear()
        colorSet.clear()
    }
}