package client.controllers

import client.models.ClientContext
import client.models.ClientContextImpl
import javafx.beans.property.SimpleStringProperty
import tornadofx.Controller

class ConnectController : Controller() {
    private val clientContext: ClientContext = ClientContextImpl.getInstance()
    val usernameProperty = SimpleStringProperty("")
    val serverAddressProperty = SimpleStringProperty("")
    val passwordProperty = SimpleStringProperty("")

    fun validateUsername(username: String): Boolean {
        return username.matches("^\\w+\$".toRegex())
    }

    fun signInRequest(success: () -> Unit, error: (String?) -> Unit) {
        if (usernameProperty.value.isNullOrEmpty() || serverAddressProperty.value.isNullOrEmpty()) {
            error(null)
            return
        } else if (usernameProperty.value.length >= 25) {
            error(null)
            return
        } else if (!validateUsername(usernameProperty.value)) {
            error(null)
            return
        }
        runAsync {
            clientContext.requestSignInToken(
                usernameProperty.value,
                serverAddressProperty.value,
                passwordProperty.value,
                {
                    ui {
                        success()
                    }
                }, {
                    val message = it
                    ui {
                        error(message)
                    }
                }
            )
        }
    }
}