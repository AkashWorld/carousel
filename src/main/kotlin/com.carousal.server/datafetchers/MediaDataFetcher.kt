package com.carousal.server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import com.carousal.server.GraphQLContext
import com.carousal.server.model.*
import java.util.concurrent.ConcurrentLinkedQueue

class MediaDataFetchers(
    private val usersRepository: UsersRepository
) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

    fun mutationPlay(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("${context.user.username}: Play")
            usersRepository.getAllUsers().parallelStream().forEach {
                it.getMediaActionPublisher()?.publishPlay(context.user.username)
            }
            if (!usersRepository.isEveryoneReady()) {
                usersRepository.getAllUsers().forEach {
                    it.getNotificationPublisher()?.publishNotification(
                        Notification(
                            "Not everyone is ready! Initiate" +
                                    " a ready check by pressing the ready check button in the bottom of the chat."
                        )
                    )
                }
            }
            return@DataFetcher false
        }
    }

    fun mutationPause(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("${context.user.username}: Pause")
            usersRepository.getAllUsers().parallelStream().forEach {
                it.getMediaActionPublisher()?.publishPause(context.user.username)
            }
            return@DataFetcher true
        }
    }

    fun mutationLoad(): DataFetcher<Media?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val file: String = environment.getArgument("file")
            context.user.media = Media(file)
            logger.info("${context.user.username}: Loading file $file")
            usersRepository.getAllUsers().parallelStream().forEach {
                it.getUserActionPublisher()?.publishUserActionEvent(context.user, UserAction.CHANGE_MEDIA)
            }
            return@DataFetcher Media(file)
        }
    }

    fun mutationSeek(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("${context.user.username}: Seek at time $currentTime")
            usersRepository.getAllUsers().parallelStream().forEach {
                it.getMediaActionPublisher()?.publishSeek(context.user.username, currentTime)
            }
            return@DataFetcher true
        }

    }

    fun subscriptionMedia(): DataFetcher<Publisher<MediaSubscriptionResult>> {
        return DataFetcher {
            val context = it.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("subscriptionChatFeed: No context found")
                throw Exception("subscriptionChatFeed: No context found")
            }
            val mediaActionPublisher = MediaActionPublisher()
            context.user.setMediaActionPublisher(mediaActionPublisher)
            mediaActionPublisher
        }
    }
}

class MediaActionPublisher : Publisher<MediaSubscriptionResult> {
    private var subscriber: Subscriber<in MediaSubscriptionResult>? = null

    override fun subscribe(subscriber: Subscriber<in MediaSubscriptionResult>?) {
        this.subscriber = subscriber
    }

    fun publishPlay(user: String) {
        subscriber?.onNext(MediaSubscriptionResult(Action.PLAY, null, user))
    }

    fun publishPause(user: String) {
        subscriber?.onNext(MediaSubscriptionResult(Action.PAUSE, null, user))
    }

    fun publishSeek(user: String, currentTime: Float) {
        subscriber?.onNext(MediaSubscriptionResult(Action.SEEK, currentTime, user))
    }
}