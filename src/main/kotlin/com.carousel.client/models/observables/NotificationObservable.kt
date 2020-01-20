package com.carousel.client.models.observables

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

data class Notification(val content: String)

class NotificationObservable : ObservableValue<Notification> {
    private val listeners: MutableList<ChangeListener<in Notification>> = mutableListOf()
    private var notification: Notification? = null

    override fun removeListener(listener: ChangeListener<in Notification>?) {
        listener?.run { listeners.remove(this) }
    }

    override fun removeListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listener: ChangeListener<in Notification>?) {
        listener?.run { listeners.add(this) }
    }

    override fun getValue(): Notification? {
        return notification
    }

    fun setValue(notification: Notification) {
        listeners.forEach { it.changed(this, this.notification, notification) }
        this.notification = notification
    }
}