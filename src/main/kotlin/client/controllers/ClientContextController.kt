package client.controllers

import client.models.ClientContext
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController : Controller() {
    private val clientContext: ClientContext by param()

    fun getAddress(): String {
        return clientContext.getServerAddress() ?: "Error"
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress())
        clipboard.setContent(content)
    }
}