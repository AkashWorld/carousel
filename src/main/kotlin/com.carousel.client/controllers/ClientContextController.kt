package com.carousel.client.controllers

import com.carousel.client.models.ClientContext
import com.carousel.client.models.ClientContextImpl
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController : Controller() {
    private val clientContext: ClientContext = ClientContextImpl.getInstance()

    fun getAddress(): String {
        return clientContext.getServerAddress() ?: "Error"
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress())
        clipboard.setContent(content)
    }
}