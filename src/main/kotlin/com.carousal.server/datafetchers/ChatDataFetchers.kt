package com.carousal.server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import com.carousal.server.GraphQLContext
import com.carousal.server.model.ChatRepository
import com.carousal.server.model.ContentType
import com.carousal.server.model.Message
import java.util.concurrent.ConcurrentLinkedQueue

class ChatDataFetchers(private val chatRepository: ChatRepository) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val chatFeedPublisher: ChatFeedPublisher = ChatFeedPublisher()

    fun queryGetMessagePaginated(): DataFetcher<List<Message>> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("queryGetMessagePaginated: No context found")
            }
            val start = environment.getArgument<Int>("start")
            val count = environment.getArgument<Int>("count")
            chatRepository.getPaginatedMessages(start, count)
        }
    }

    fun queryGetLengthOfChatFeed(): DataFetcher<Int> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("queryGetLengthOfChatFeed: No context found")
            }
            chatRepository.getNumberOfMessages()
        }
    }

    fun mutationInsertMessage(): DataFetcher<Boolean> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("mutationInsertMessage: No context found")
            }
            val message = environment.getArgument<String>("message")
            chatFeedPublisher.publishMessage(chatRepository.addMessage(context.user, message, ContentType.MESSAGE))
            true
        }
    }

    fun mutationInsertImage(): DataFetcher<Boolean> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("mutationInsertImage: No context found")
            }
            val data = environment.getArgument<String>("data")
            chatFeedPublisher.publishMessage(chatRepository.addMessage(context.user, data, ContentType.IMAGE))
            true
        }
    }

    fun subscriptionChatFeed(): DataFetcher<Publisher<Message>> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("subscriptionChatFeed: No context found")
                throw Exception("subscriptionChatFeed: No context found")
            }
            chatFeedPublisher
        }
    }
}

/**
 * Memory leak, we need to figure out how to clean this up if user disconnects
 */
class ChatFeedPublisher : Publisher<Message> {
    private val subscribers: ConcurrentLinkedQueue<Subscriber<in Message>?> = ConcurrentLinkedQueue()

    override fun subscribe(s: Subscriber<in Message>?) {
        subscribers.add(s)
    }

    fun publishMessage(message: Message) {
        subscribers.forEach {
            it?.onNext(message)
        }
    }
}