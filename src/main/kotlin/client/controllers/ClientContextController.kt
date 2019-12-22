package client.controllers

import client.models.ClientContext
import javafx.beans.value.ObservableStringValue
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController: Controller() {
    private val clientContext: ClientContext = ClientContext()

    fun getAddress(): ObservableStringValue {
        return clientContext.getServerAddress()
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress().get())
        clipboard.setContent(content)
    }

    private fun getHttpAddress(): String {
        return "http://${clientContext.getServerAddress().get()}"
    }

    private fun getWebSocketAddress(): String {
        return "ws://${clientContext.getServerAddress().get()}"
    }
}