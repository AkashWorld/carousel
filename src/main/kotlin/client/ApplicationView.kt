package client

import client.models.ClientContextImpl
import tornadofx.View
import tornadofx.stackpane

class ApplicationView : View() {
    companion object {
        val APPLICATION_NAME = "Playtime"
    }
    private val clientContext = ClientContextImpl()

    override val root = stackpane {

    }
}