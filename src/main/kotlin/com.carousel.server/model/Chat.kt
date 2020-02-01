package com.carousel.server.model

import java.lang.IndexOutOfBoundsException
import java.time.Instant
import java.util.concurrent.ConcurrentLinkedDeque

enum class ContentType {
    IMAGE,
    IMAGE_URL,
    MESSAGE,
    INFO
}

data class Message(val contentType: ContentType, val content: String, val username: String, val timestamp: String)

class ChatRepository {
    /**
     * We probably don't need to keep more than 50 in a server. Since this is meant
     * to run on a commodity/personal machine, no need to take up additional space,
     * especially if there are messages with images coming in.
     */
    private val messages = ConcurrentLinkedDeque<Message>()

    fun addMessage(user: User, content: String, contentType: ContentType): Message {
        val message = Message(contentType, content, user.username, Instant.now().epochSecond.toString())
        //TODO: This is O(n) in a concurrent deque, need to figure out a better way
        while (messages.size > 50) {
            messages.removeFirst()
        }
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