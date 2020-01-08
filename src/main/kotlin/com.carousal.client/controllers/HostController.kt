package com.carousal.client.controllers

import com.carousal.client.views.ApplicationView
import com.carousal.client.models.ClientContextImpl
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import com.carousal.server.Server
import tornadofx.*

class HostController : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val context = ClientContextImpl.getInstance()
    val usernameProperty = SimpleStringProperty("")
    val passwordProperty = SimpleStringProperty("")

    fun hostNewServer(success: () -> Unit, error: (String?) -> Unit) {
        if (usernameProperty.value.isNullOrEmpty()) {
            runLater { error(null) }
            return
        } else if (!validateUsername(usernameProperty.value)) {
            runLater { error(null) }
            return
        } else if (usernameProperty.value.length > 25) {
            runLater { error(null) }
            return
        }
        runAsync {
            val server = Server.getInstance()
            if (!passwordProperty.value.isNullOrEmpty()) {
                server.setServerPassword(passwordProperty.value)
            }
            val externalIp: String
            try {
                server.initPortForwarding()
                server.initialize()
                externalIp = server.getExternalIP()
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                val message: String = "Could not intialize server, please ensure you do not have more " +
                        "than one instance of ${ApplicationView.APPLICATION_NAME} running. In addition, please ensure that " +
                        "your network has Universal Plug and Play (UPnP) enabled."
                ui {
                    error(message)
                }
                server.close()
                return@runAsync
            }
            val password = if (passwordProperty.value.isNullOrEmpty()) null else passwordProperty.value
            context.requestSignInToken(usernameProperty.value, externalIp, password, {
                ui {
                    success()
                }
            }, {
                val message = it
                server.close()
                ui {
                    error(message)
                }
            }
            )
        }
    }

    fun validateUsername(username: String): Boolean {
        return username.matches("^\\w+\$".toRegex())
    }
}