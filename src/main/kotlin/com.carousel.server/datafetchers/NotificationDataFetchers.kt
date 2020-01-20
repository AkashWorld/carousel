package com.carousel.server.datafetchers

import com.carousel.server.GraphQLContext
import com.carousel.server.model.Notification
import graphql.schema.DataFetcher
import org.reactivestreams.Publisher
import org.reactivestreams.Subscriber
import org.slf4j.LoggerFactory

class NotificationDataFetchers {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

    fun subscriptionNotification(): DataFetcher<NotificationPublisher> {
        return DataFetcher {
            val context = it.getContext<GraphQLContext?>()
            if (context == null) {
                logger.error("notificationSubscription: No context found")
                throw Exception("notificationSubscription: No context found")
            }
            val notificationPublisher = NotificationPublisher()
            context.user.setNotificationPublisher(notificationPublisher)
            notificationPublisher
        }
    }
}

class NotificationPublisher : Publisher<Notification> {
    private var subscriber: Subscriber<in Notification>? = null

    override fun subscribe(s: Subscriber<in Notification>?) {
        subscriber = s
    }

    fun publishNotification(notification: Notification) {
        subscriber?.onNext(notification)
    }
}
