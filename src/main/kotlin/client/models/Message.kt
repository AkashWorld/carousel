package client.models

import javafx.collections.ObservableList
import tornadofx.toObservable

enum class ContentType {
    IMAGE,
    MESSAGE,
    INFO,
    NONE
}

class Message(
    val user: String,
    val content: String,
    val contentType: ContentType = ContentType.MESSAGE
) {
}

class ChatModel() {
    private val chatList: ObservableList<Message> = mutableListOf<Message>().toObservable()

    fun addMessage(message: Message) {
        this.chatList.add(message)
    }

    fun getChatList(): ObservableList<Message> {
        return this.chatList
    }
}