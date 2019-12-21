package server.datafetchers

import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory
import server.GraphQLContext
import server.model.Action
import server.model.Media
import server.model.MediaSubscriptionResult

class MediaDataFetchers {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private val mediaActionPublisher = MediaActionPublisher()
    fun mutationPlay(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("${context.user.getUsername()}: Play at time $currentTime")
            mediaActionPublisher.publishPlay(context.user.getUsername(), currentTime)
            return@DataFetcher true
        }
    }

    fun mutationPause(): DataFetcher<Boolean?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val currentTime: Float = environment.getArgument("currentTime")
            logger.info("${context.user.getUsername()}: Pause at time $currentTime")
            mediaActionPublisher.publishPause(context.user.getUsername(), currentTime)
            return@DataFetcher true
        }
    }

    fun mutationLoad(): DataFetcher<Media?> {
        return DataFetcher { environment ->
            val context: GraphQLContext = environment.getContext() ?: return@DataFetcher null
            val file: String = environment.getArgument("file")
            logger.info("${context.user.getUsername()}: Loading file $file")
            return@DataFetcher Media(file)
        }
    }

    fun mediaSubscription(): DataFetcher<Publisher<MediaSubscriptionResult>> {
        return DataFetcher {
            this.mediaActionPublisher
        }
    }
}

class MediaActionPublisher: Publisher<MediaSubscriptionResult> {
    private var subscriber: Subscriber<in MediaSubscriptionResult>? = null

    override fun subscribe(subscriber: Subscriber<in MediaSubscriptionResult>?) {
        this.subscriber = subscriber
    }

    fun publishPlay(user: String, currentTime: Float) {
        subscriber?.onNext(MediaSubscriptionResult(Action.PLAY, currentTime, user))
    }

    fun publishPause(user: String, currentTime: Float) {
        subscriber?.onNext(MediaSubscriptionResult(Action.PAUSE, currentTime, user))
    }
}