package client.playerpage

import client.playerpage.chatfeed.ChatView
import javafx.geometry.Pos
import tornadofx.*


class FileLoaderView: View() {
    private val fileLoaderController: FileLoaderController by inject()
    private val chatView = find(ChatView::class)

    override val root = vbox {
        prefWidth = 1600.0
        prefHeight = 1200.0
        alignment = Pos.CENTER
        text("PLAYTIME") {
            style {
                fontSize = 250.px
            }
        }
        button("Load Video") {
            style {
                fontSize = 25.px
                backgroundRadius = multi(box(50.px))
                prefWidth = 300.px
                prefHeight = 60.px
            }
            action {
                fileLoaderController.loadVideoFile()
                replaceWith<MediaPlayerView>()
            }
        }
        chatView
    }
}
