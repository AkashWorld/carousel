package client.playerpage

import client.models.ClientContext
import client.playerpage.chatfeed.ChatView
import javafx.scene.layout.HBox
import javafx.scene.paint.LinearGradient
import tornadofx.*

class PlayerPage : View() {

    private val fileLoaderView: FileLoaderView by inject()
    private val clientContext = ClientContext("localhost:57423")
    private val chatView: ChatView by inject(params = mapOf("clientContext" to clientContext))

    override val root = HBox()

    init {
        with(root) {
            this.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
            this.add(fileLoaderView)
            this.add(chatView)
        }
        reloadStylesheetsOnFocus()
    }
}
