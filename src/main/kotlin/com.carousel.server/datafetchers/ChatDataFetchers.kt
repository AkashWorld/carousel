package com.carousel.server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import com.carousel.server.GraphQLContext
import com.carousel.server.model.ChatRepository
import com.carousel.server.model.ContentType
import com.carousel.server.model.Message
import com.carousel.server.model.UsersRepository

class ChatDataFetchers(private val usersRepository: UsersRepository, private val chatRepository: ChatRepository) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

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
            publishToChatFeed(chatRepository.addMessage(context.user, message, ContentType.MESSAGE))
            true
        }
    }

    fun mutationInsertImageUrl(): DataFetcher<Boolean> {
        return DataFetcher { environment ->
            val context = environment.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("queryGetMessagePaginated: No context found")
                throw Exception("mutationInsertImage: No context found")
            }
            val data = environment.getArgument<String>("data")
            publishToChatFeed(chatRepository.addMessage(context.user, data, ContentType.IMAGE_URL))
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
            publishToChatFeed(chatRepository.addMessage(context.user, data, ContentType.IMAGE))
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
            val chatFeedPublisher = ChatFeedPublisher()
            context.user.setChatFeedPublisher(chatFeedPublisher)
            chatFeedPublisher
        }
    }

    private fun publishToChatFeed(message: Message) {
        usersRepository.getAllUsers().parallelStream().forEach {
            it.getChatFeedPublisher()?.publishMessage(message)
        }
    }
}

class ChatFeedPublisher : Publisher<Message> {
    private var subscriber: Subscriber<in Message>? = null

    override fun subscribe(s: Subscriber<in Message>?) {
        subscriber = s
    }

    fun publishMessage(message: Message) {
        subscriber?.onNext(message)
    }
}