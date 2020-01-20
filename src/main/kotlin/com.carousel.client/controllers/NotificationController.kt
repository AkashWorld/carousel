package com.carousel.client.controllers

import com.carousel.client.models.NotificationModel
import com.carousel.client.models.observables.NotificationObservable
import tornadofx.*

class NotificationController: Controller() {
    private val notificationModel = NotificationModel()

    fun getNotificationListener(): NotificationObservable {
        return notificationModel.getNotificationObservable()
    }

    fun sendNotificationSubscription(error: () -> Unit) {
        notificationModel.subscribeToNotifications(error)
    }
}