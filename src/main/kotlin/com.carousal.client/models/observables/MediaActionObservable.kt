package com.carousal.client.models.observables

import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

enum class Action {
    PLAY,
    PAUSE,
    SEEK
}

data class MediaAction(val action: Action, val currentTime: Float?, val user: String)

class MediaActionObservable(action: MediaAction?) : ObservableValue<MediaAction?> {
    private var currentMediaAction: MediaAction? = null
    private val listeners: MutableList<ChangeListener<in MediaAction?>> = mutableListOf()

    init {
        currentMediaAction = action
    }

    override fun addListener(listener: ChangeListener<in MediaAction?>?) {
        listener?.run { listeners.add(listener) }
    }

    override fun removeListener(listener: ChangeListener<in MediaAction?>?) {
        listener?.run { listeners.remove(listener) }
    }

    override fun getValue(): MediaAction? {
        return currentMediaAction
    }

    fun setValue(action: MediaAction) {
        listeners.forEach {
            it.changed(this, currentMediaAction, action)
        }
        currentMediaAction = action
    }

    /**
     * Not implemented
     */
    override fun removeListener(listener: InvalidationListener?) {}

    /**
     * Not implemented
     */
    override fun addListener(listener: InvalidationListener?) {}
}