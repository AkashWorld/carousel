package com.carousel.client.controllers

import com.carousel.client.models.ClientContext
import com.carousel.client.models.ClientContextImpl
import javafx.scene.input.ClipboardContent
import tornadofx.Controller

class ClientContextController : Controller() {
    private val clientContext: ClientContext = ClientContextImpl.getInstance()

    fun getAddress(): String {
        var address = clientContext.getServerAddress() ?: return "Error"
        if (address.length >= 35) {
            address = address.substring(0, 35) + "..."
        }
        return address
    }

    fun addressToClipboard() {
        val content = ClipboardContent()
        content.putString(clientContext.getServerAddress())
        clipboard.setContent(content)
    }
}