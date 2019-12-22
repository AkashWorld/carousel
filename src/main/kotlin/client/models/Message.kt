package client.models

import javafx.collections.ObservableList
import tornadofx.toObservable

enum class ContentType {
    IMAGE,
    MESSAGE
}

class Content(
    private val contentType: ContentType,
    private val content: Any
)

class Message(
    private val user: String,
    private val content: String,
    private val contentType: ContentType = ContentType.MESSAGE
) {
    fun getUsername(): String {
        return user
    }

    fun getContent(): String {
        return content
    }
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