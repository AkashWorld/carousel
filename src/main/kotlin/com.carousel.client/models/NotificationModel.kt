package com.carousel.client.models

import com.carousel.client.models.observables.Notification
import com.carousel.client.models.observables.NotificationObservable
import com.google.gson.Gson
import org.slf4j.LoggerFactory
import tornadofx.runLater

private data class NotificationResponse(val notification: Notification)

class NotificationModel {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val notificationObservable = NotificationObservable()

    fun getNotificationObservable(): NotificationObservable {
        return notificationObservable
    }

    fun subscribeToNotifications(error: () -> Unit) {
        val query = """
            subscription {
                notification {
                    content
                }
            }
        """.trimIndent()
        ClientContextImpl.getInstance().sendSubscriptionRequest(query, emptyMap(), {
            try {
                val notificationResponse = Gson().fromJson(it, NotificationResponse::class.java)
                runLater {
                    notificationResponse?.notification?.run {
                        notificationObservable.setValue(this)
                    }
                }
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                runLater(error)

            }
        }, { runLater(error) })
    }
}