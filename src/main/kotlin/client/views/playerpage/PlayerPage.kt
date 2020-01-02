package client.views.playerpage

import client.controllers.ChatController
import client.views.playerpage.chatfeed.ChatView
import tornadofx.*

class PlayerPage : View() {

    private val fileLoaderView: FileLoaderView by inject()
    private val chatController: ChatController by inject()
    private val chatView: ChatView by inject()

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
