package client.playerpage

import client.controllers.ChatController
import client.models.ClientContext
import client.playerpage.chatfeed.ChatView
import tornadofx.*

class PlayerPage : View() {

    private val fileLoaderView: FileLoaderView by inject()
    private val clientContext: ClientContext by param()
    private val chatController: ChatController by inject()
    private val chatView: ChatView by inject(params = mapOf("clientContext" to clientContext))

    override val root = borderpane()

    init {
        with(root) {
            this.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
            center {
                this.add(fileLoaderView)
            }
            right {
                this.add(chatView)
                chatController.isChatShown().addListener { _, _, newValue ->
                    if (newValue) {
                        this.children.clear()
                    } else {
                        this.add(chatView)
                    }
                }
            }
        }
        reloadStylesheetsOnFocus()
    }
}
