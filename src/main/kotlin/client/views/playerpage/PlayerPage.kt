package client.views.playerpage

import client.controllers.ChatController
import client.views.playerpage.chatfeed.ChatView
import client.views.playerpage.mediaplayer.MediaPlayerView
import javafx.scene.layout.Priority
import tornadofx.*

class PlayerPage : View() {

    private val fileLoaderView: FileLoaderView by inject()
    private val chatController: ChatController by inject()
    private val chatView: ChatView by inject()

    override val root = borderpane {
        setMinSize(0.0, 0.0)
        center {
            this.add(fileLoaderView)
        }
        right {
            this.add(chatView)
            chatController.isChatShown().addListener { _, _, newValue ->
                if (!newValue) {
                    this.children.remove(chatView.root)
                } else {
                    this.right = chatView.root
                }
            }
        }
    }
}
