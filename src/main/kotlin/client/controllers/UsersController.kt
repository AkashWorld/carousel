package client.controllers

import client.models.UserObservable
import client.models.UsersModel
import javafx.collections.ObservableList
import tornadofx.*

class UsersController : Controller() {
    private val usersModel: UsersModel = UsersModel()

    fun subscribeToUsersAction(error: () -> Unit) {
        usersModel.subscribeToUserActions { error() }
    }

    fun sendReadyCheck(isReady: Boolean, success: (Boolean) -> Unit, error: () -> Unit) {
        usersModel.sendReadyCheck(isReady, success, error)
    }

    fun getUsersList(): ObservableList<UserObservable> {
        return usersModel.getUsers()
    }
}