package client.controllers

import client.models.ClientContext
import javafx.beans.value.ObservableStringValue
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController(private val clientContext: ClientContext): Controller() {

    fun getAddress(): ObservableStringValue {
        return clientContext.getServerAddress()
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress().get())
        clipboard.setContent(content)
    }
}