package client.playerpage

import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ChatModel
import client.models.ClientContext
import client.playerpage.chatfeed.ChatView
import javafx.beans.property.SimpleStringProperty
import javafx.scene.paint.LinearGradient
import tornadofx.*

class PlayerPage : View() {

    private val fileLoaderView: FileLoaderView by inject()
    private val clientContext = ClientContext(SimpleStringProperty("localhost:57423"), "")
    private val chatView = ChatView(ChatController(ChatModel(), clientContext), ClientContextController(clientContext))

    override val root = hbox {
        this.add(fileLoaderView)
        this.add(chatView)
    }

    init {
        reloadStylesheetsOnFocus()
    }
}

val mainGradient: LinearGradient = LinearGradient.valueOf("from 0% 0% to 100% 100%, #3c0054, #0b000f")

