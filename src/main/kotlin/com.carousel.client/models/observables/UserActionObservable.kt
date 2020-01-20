package com.carousel.client.models.observables

import com.carousel.client.models.UserActionEvent
import javafx.beans.InvalidationListener
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue

class UserActionObservable :
    ObservableValue<UserActionEvent> {
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