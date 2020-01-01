package client.controllers

import client.models.Client
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller

class ConnectController : Controller() {
    private val clientContext = Client.clientContext
    val usernameProperty = SimpleStringProperty("")
    val serverAddressProperty = SimpleStringProperty("")
    val passwordProperty = SimpleStringProperty("")

    fun validateUsername(username: String): Boolean {
        return username.matches("^\\w+\$".toRegex())
    }

    fun signInRequest(success: () -> Unit, error: (String?) -> Unit) {
        if (usernameProperty.value.isNullOrEmpty() || serverAddressProperty.value.isNullOrEmpty()) {
            error("Username or Server Address must not be empty!")
            return
        } else if (usernameProperty.value.length > 25) {
            error("Username length must be less than 25")
            return
        } else if (!validateUsername(usernameProperty.value)) {
            error("Only alphanumeric characters are allowed for the username")
            return
        }
        clientContext.requestSignInToken(
            usernameProperty.value,
            serverAddressProperty.value,
            passwordProperty.value,
            success,
            error
        )
    }
}