package client.controllers

import client.models.ClientContext
import javafx.beans.value.ObservableStringValue
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController : Controller() {
    private val clientContext: ClientContext by param()

    init {
    }

    fun getAddress(): String {
        return clientContext.getServerAddress()
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress())
        clipboard.setContent(content)
    }
}