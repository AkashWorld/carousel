package com.carousal.client.controllers

import com.carousal.client.models.UserActionObservable
import com.carousal.client.models.UserObservable
import com.carousal.client.models.UsersModel
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

    fun sendReadyCheck(success: () -> Unit, error: () -> Unit) {
        usersModel.sendReadyCheck(!isReady.value, {
            if (!it) {
                mediaController.pauseAction { runLater(error) }
            }
            isReady.value = it
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