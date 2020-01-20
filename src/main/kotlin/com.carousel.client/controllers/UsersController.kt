package com.carousel.client.controllers

import com.carousel.client.models.observables.UserActionObservable
import com.carousel.client.models.observables.UserObservable
import com.carousel.client.models.UsersModel
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.ObservableList
import tornadofx.*

class UsersController : Controller() {
    private val mediaController: MediaController by inject()
    private val usersModel: UsersModel = UsersModel()
    val isReady = SimpleBooleanProperty(false)

    fun subscribeToUsersAction(error: () -> Unit) {
        usersModel.subscribeToUserActions { error() }
    }

    fun sendIsReady(success: () -> Unit, error: () -> Unit) {
        usersModel.sendIsReady(!isReady.value, {
            if (!it) {
                mediaController.pauseAction { runLater(error) }
            }
            isReady.value = it
            runLater(success)
        }, { runLater(error) })
    }

    fun sendIsReady(value: Boolean, success: () -> Unit, error: () -> Unit) {
        usersModel.sendIsReady(value, {
            if (!it) {
                mediaController.pauseAction { runLater(error) }
            }
            isReady.value = it
            runLater(success)
        }, { runLater(error) })
    }

    fun sendInitiateReadyCheck(success: () -> Unit, error: () -> Unit) {
        usersModel.sendInitiateReadyCheck({
            runLater(success)
        }, { runLater(error) })
    }

    fun getUsersList(): ObservableList<UserObservable> {
        return usersModel.getUsers()
    }

    fun getUserActionObservable(): UserActionObservable {
        return usersModel.getUserActionObservable()
    }
}