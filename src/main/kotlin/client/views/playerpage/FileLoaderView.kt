package client.views.playerpage

import client.views.ApplicationView
import client.controllers.FileLoaderController
import client.views.playerpage.FileLoaderStyles.Companion.mainGradient
import client.views.playerpage.mediaplayer.MediaPlayerView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*


class FileLoaderView : View() {
    private val fileLoaderController: FileLoaderController by inject()
    private var container: VBox? = null
    private var loadingErrorMessage: SimpleStringProperty = SimpleStringProperty("")

    override val root = vbox {
        isFillWidth = true
        container = this
        alignment = Pos.CENTER
        style {
            this.backgroundColor = multi(mainGradient)
        }
        text(ApplicationView.APPLICATION_NAME.toUpperCase()) {
            addClass(FileLoaderStyles.titleText)
        }
        button("Load Video") {
            addClass(FileLoaderStyles.loadVideoButton)
            action {
                val result = fileLoaderController.loadVideoFile()
                if (result) {
                    replaceWith<MediaPlayerView>(ViewTransition.Fade(1000.millis))
                } else {
                    loadingErrorMessage.set("Please select a media file to play!")
                }
            }
        }
        text(loadingErrorMessage) {
            style {
                this.fill = Color.WHITE
            }
        }
    }
}
