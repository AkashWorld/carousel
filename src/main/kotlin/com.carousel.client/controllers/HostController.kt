package com.carousel.client.controllers

import com.carousel.client.models.ClientContext
import com.carousel.client.models.ClientContextImpl
import javafx.beans.property.SimpleStringProperty
import org.slf4j.LoggerFactory
import com.carousel.server.Server
import tornadofx.*

class HostController : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val context = ClientContextImpl.getInstance()
    val usernameProperty = SimpleStringProperty("")
    val portForwardingProperty = SimpleStringProperty("")
    val passwordProperty = SimpleStringProperty("")

    fun hostNewServer(success: () -> Unit, error: (String?) -> Unit) {
        val port = getValidPortAsInt(portForwardingProperty.value)
        if (usernameProperty.value.isNullOrEmpty()) {
            runLater { error(null) }
            return
        } else if (!validateUsername(usernameProperty.value)) {
            runLater { error(null) }
            return
        } else if (usernameProperty.value.length > 25) {
            runLater { error(null) }
            return
        } else if (!portForwardingProperty.value.isNullOrEmpty() && port == null) {
            runLater { error("Port is not a valid value. Please make sure that the port number is between 1024 and 65535.") }
            return
        }
        runAsync {
            val server = Server.getInstance()
            if (!passwordProperty.value.isNullOrEmpty()) {
                server.setServerPassword(passwordProperty.value)
            }
            val externalIp: String
            val message: String =
                "Could not initialize server. Please make sure there is no more than" +
                        " one instance of this application running and that Universal Plug and Play (UPnP) is enabled in your router. " +
                        "If it is not possible to enable UPnP, try enabling manual port forwarding and " +
                        "and attempt to host again with the specified port."
            try {
                if (port == null) {
                    val upnpSuccessful = server.initPortForwarding()
                    if (!upnpSuccessful) {
                        throw Exception("Unable to initialize UPnP port forwading.")
                    }
                    server.initialize()
                } else {
                    server.initialize(port)
                }
                externalIp = server.getExternalIP()
            } catch (e: Exception) {
                logger.error(e.message, e.cause)
                ui {
                    error(message)
                }
                server.close()
                return@runAsync
            }
            var finalAddress = externalIp
            finalAddress += if (port != null) {
                ":" + portForwardingProperty.value
            } else {
                ":" + ClientContext.DEFAULT_PORT
            }
            val password = if (passwordProperty.value.isNullOrEmpty()) null else passwordProperty.value
            context.requestSignInToken(usernameProperty.value, finalAddress, password, {
                ui {
                    success()
                }
            }, {
                val httpErrorMessage = it
                server.close()
                ui {
                    if (port != null) {
                        error(httpErrorMessage)
                    } else {
                        error(message)
                    }
                }
            }
            )
        }
    }

    private fun getValidPortAsInt(port: String): Int? {
        try {
            val intPort = Integer.parseInt(port)
            if (intPort < 1024 || intPort >= 65535) {
                return null
            }
            return intPort
        } catch (e: Exception) {
            return null
        }
    }

    fun validateUsername(username: String): Boolean {
        return username.matches("^\\w+\$".toRegex())
    }

    fun clear() {
        usernameProperty.value = ""
        passwordProperty.value = ""
        portForwardingProperty.value = ""
    }
}