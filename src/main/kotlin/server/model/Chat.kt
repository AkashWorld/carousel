package server.model

import java.time.Instant
import java.util.concurrent.atomic.AtomicReference

enum class ContentType {
    IMAGE,
    MESSAGE
}

data class Message(val contentType: ContentType, val content: String, val username: String, val timestamp: String)

class ChatFeedRepository {
    private val messages = AtomicReference(mutableListOf<Message>())

    fun addMessage(user: User, content: String, contentType: ContentType): Message {
        val message = Message(contentType, content, user.getUsername(), Instant.now().epochSecond.toString())
        messages.get().add(message)
        return message
    }

    /**
     * Backwards pagination
     */
    fun getPaginatedMessages(begin: Int, count: Int): List<Message> {
        val end = messages.get().size - 1 - begin
        val start = end - count
        if (end - start < 0) {
            throw IndexOutOfBoundsException("Pagination out of bounds, ${end + 1 + begin} messages")
        }
        return messages.get().slice(start..end)
    }

    fun getNumberOfMessages(): Int {
        return messages.get().size
    }
}