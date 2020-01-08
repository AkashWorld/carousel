package server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.ChatFeedRepository
import server.model.ContentType
import server.model.Message
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicReference

class ChatFeedDataFetchers(private val chatFeed: ChatFeedRepository) {
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
            chatFeed.getPaginatedMessages(start, count)
        }
    }

    fun queryGetLengthOfChatFeed(): DataFetcher<Int> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("queryGetLengthOfChatFeed: No context found")
            }
            chatFeed.getNumberOfMessages()
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
            chatFeedPublisher.publishMessage(chatFeed.addMessage(context.user, message, ContentType.MESSAGE))
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
            chatFeedPublisher.publishMessage(chatFeed.addMessage(context.user, data, ContentType.IMAGE))
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