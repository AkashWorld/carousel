package client.playerpage.chatfeed

import tornadofx.*
import javafx.collections.ObservableList
import org.slf4j.LoggerFactory
import tornadofx.toObservable

data class Message(val username: String, val message: String)

class ChatController: Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);

    private val chatMessages: ObservableList<Message> = listOf(
        Message("Lone Hunt", "Yo, What's up!"),
        Message("DaBessBeast", "Ayo fam what we watchin"),
        Message("Lone Hunt", "lol your trash we're watching you're name"),
        Message("awildwildboar", "one piece of bust"),
        Message("DaBessBeast", "This app doesn't even work lets just use syncplay, I can't take it anymore!")
    ).toObservable()

    fun getMessages(): ObservableList<Message> {
        return chatMessages
    }
}