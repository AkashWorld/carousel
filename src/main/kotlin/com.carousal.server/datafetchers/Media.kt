package com.carousal.server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import com.carousal.server.GraphQLContext
import com.carousal.server.model.Action
import com.carousal.server.model.Media
import com.carousal.server.model.MediaSubscriptionResult
import java.util.concurrent.ConcurrentLinkedQueue

class MediaDataFetchers(private val userActionPublisher: UserActionPublisher) {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val mediaActionPublisher = MediaActionPublisher()
    fun mutationPlay(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("${context.user.username}: Play")
            mediaActionPublisher.publishPlay(context.user.username)
            return@DataFetcher true
        }
    }

    fun mutationPause(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            logger.info("${context.user.username}: Pause")
            mediaActionPublisher.publishPause(context.user.username)
            return@DataFetcher true
        }
    }

    fun mutationLoad(): DataFetcher<Media?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val file: String = environment.getArgument("file")
            context.user.media = Media(file)
            logger.info("${context.user.username}: Loading file $file")
            userActionPublisher.publishUserActionEvent(context.user, UserAction.CHANGE_MEDIA)
            return@DataFetcher Media(file)
        }
    }

    fun mutationSeek(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("${context.user.username}: Seek at time $currentTime")
            mediaActionPublisher.publishSeek(context.user.username, currentTime)
            return@DataFetcher true
        }

    }

    fun subscriptionMedia(): DataFetcher<Publisher<MediaSubscriptionResult>> {
        return DataFetcher {
            this.mediaActionPublisher
        }
    }
}

class MediaActionPublisher : Publisher<MediaSubscriptionResult> {
    private val subscribers: ConcurrentLinkedQueue<Subscriber<in MediaSubscriptionResult>?> =
        ConcurrentLinkedQueue()

    override fun subscribe(subscriber: Subscriber<in MediaSubscriptionResult>?) {
        subscribers.add(subscriber)
    }

    fun publishPlay(user: String) {
        subscribers.forEach {
            it?.onNext(MediaSubscriptionResult(Action.PLAY, null, user))
        }
    }

    fun publishPause(user: String) {
        subscribers.forEach {
            it?.onNext(MediaSubscriptionResult(Action.PAUSE, null, user))
        }
    }

    fun publishSeek(user: String, currentTime: Float) {
        subscribers.forEach {
            it?.onNext(MediaSubscriptionResult(Action.SEEK, currentTime, user))
        }
    }
}