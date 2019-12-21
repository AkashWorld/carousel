package server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.ChatFeedRepository
import server.model.ContentType
import server.model.Message
import java.util.concurrent.atomic.AtomicReference
import javax.security.sasl.AuthenticationException

class ChatFeedDataFetchers(private val chatFeed: ChatFeedRepository) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private val chatFeedPublisher: ChatFeedPublisher = ChatFeedPublisher()

    fun queryGetMessagePaginated(): DataFetcher<List<Message>> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw AuthenticationException("queryGetMessagePaginated: No context found")
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
                throw AuthenticationException("queryGetMessagePaginated: No context found")
            }
            chatFeed.getNumberOfMessages()
        }
    }

    fun mutationInsertMessage(): DataFetcher<Boolean> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw AuthenticationException("queryGetMessagePaginated: No context found")
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
                throw AuthenticationException("queryGetMessagePaginated: No context found")
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
                logger.error("queryGetMessagePaginated: No context found")
                throw AuthenticationException("queryGetMessagePaginated: No context found")
            }
            chatFeedPublisher
        }
    }
}

class ChatFeedPublisher : Publisher<Message> {
    private val subscriber: AtomicReference<Subscriber<in Message>?> = AtomicReference()

    override fun subscribe(s: Subscriber<in Message>?) {
        subscriber.set(s)
    }

    fun publishMessage(message: Message) {
        subscriber.get()?.onNext(message)
    }
}