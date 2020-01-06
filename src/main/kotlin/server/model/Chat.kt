package server.model

import java.lang.IndexOutOfBoundsException
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

enum class ContentType {
    IMAGE,
    MESSAGE,
    INFO
}

data class Message(val contentType: ContentType, val content: String, val username: String, val timestamp: String)

class ChatFeedRepository {
    private val messages = ConcurrentLinkedQueue<Message>()

    fun addMessage(user: User, content: String, contentType: ContentType): Message {
        val message = Message(contentType, content, user.username, Instant.now().epochSecond.toString())
        messages.add(message)
        return message
    }

    /**
     * Backwards pagination
     */
    fun getPaginatedMessages(begin: Int, count: Int): List<Message> {
        val end = messages.size - begin
        if (end < 0) {
            throw IndexOutOfBoundsException()
        }
        val start = end - count
        if (start < 0) {
            return messages.toList().slice(0 until end)
        }
        return messages.toList().slice(start until end)
    }

    fun getNumberOfMessages(): Int {
        return messages.size
    }

    fun clear() {
        messages.clear()
    }
}