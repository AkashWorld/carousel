package com.carousal.client.models

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

data class Media(val id: String)
data class User(val username: String, var media: Media?, var isReady: Boolean = false)

/**
 * Allows JavaFX to listen for changes in the User object (such as when User changes the media)
 */
class UserObservable(private val user: User) : ObservableValue<User> {
    private val listeners: MutableList<ChangeListener<in User>> = mutableListOf()

    override fun removeListener(listener: ChangeListener<in User>?) {
        listener?.run { listeners.remove(this) }
    }

    override fun addListener(listener: ChangeListener<in User>?) {
        listener?.run { listeners.add(this) }
    }

    override fun addListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getValue(): User {
        return user
    }

    fun setMedia(media: Media) {
        user.media = media
        listeners.forEach { it.changed(this, null, user) }
    }

    fun setIsReady(isReady: Boolean) {
        user.isReady = isReady
        listeners.forEach { it.changed(this, null, user) }
    }
}

class UserActionObservable : ObservableValue<UserActionEvent> {
    private val listeners: MutableList<ChangeListener<in UserActionEvent>> = mutableListOf()
    private var action: UserActionEvent? = null

    override fun removeListener(listener: ChangeListener<in UserActionEvent>?) {
        listener?.run { listeners.remove(this) }
    }

    override fun addListener(listener: ChangeListener<in UserActionEvent>?) {
        listener?.run { listeners.add(this) }
    }

    override fun removeListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addListener(listener: InvalidationListener?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getValue(): UserActionEvent? {
        return action
    }

    fun setValue(action: UserActionEvent) {
        this.action = action
        listeners.forEach { it.changed(this, null, this.action) }
    }

}
