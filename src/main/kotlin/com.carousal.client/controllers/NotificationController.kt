package com.carousal.client.controllers

import com.carousal.client.models.NotificationModel
import com.carousal.client.models.observables.NotificationObservable
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