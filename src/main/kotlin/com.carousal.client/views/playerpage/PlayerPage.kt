package client.views.playerpage

import client.controllers.ChatController
import client.models.ClientContextImpl
import client.views.ViewUtils
import client.views.intropage.IntroPage
import client.views.playerpage.chatfeed.ChatFragment
import javafx.scene.layout.StackPane
import tornadofx.*

class PlayerPage : View() {

    private val introPageView: IntroPage by inject()
    private val fileLoaderView: FileLoaderView by inject()
    private val chatController: ChatController by inject()

    override val root = borderpane {
        setMinSize(0.0, 0.0)
        center {
            this.add(fileLoaderView)
        }
        right {
            chatController.isChatShown().addListener { _, _, newValue ->
                if (!newValue) {
                    this.children.remove(this.right)
                } else {
                    this.right = find<ChatFragment>().root
                }
            }
        }
    }

    fun navigateToIntroPage() {
        if (isDocked) {
            root.replaceWith(introPageView.root, ViewTransition.Fade(1000.millis))
        }
    }

    fun navigateToFileLoader() {
        if (isDocked && root.center != fileLoaderView.root) {
            root.center = fileLoaderView.root
        }
    }

    override fun onDock() {
        super.onDock()
        root.right = find<ChatFragment>().root
        chatController.subscribeToMessages {
            ViewUtils.showErrorDialog("Connection with chat is lost, please restart this application.", primaryStage.scene.root as StackPane)
        }
    }

    override fun onUndock() {
        super.onUndock()
        chatController.cleanUp()
        root.center = fileLoaderView.root
        root.children.remove(root.right)
        ClientContextImpl.getInstance().clearContext()
    }
}
