package client.playerpage

import client.Styles
import client.controllers.FileLoaderController
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*


class FileLoaderView: View() {
    private val fileLoaderController: FileLoaderController by inject()
    private var container: VBox? = null
    private var loadingErrorMessage: SimpleStringProperty = SimpleStringProperty("")

    override val root = vbox {
        container = this
        setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
        alignment = Pos.CENTER
        style {
            this.backgroundColor = multi(mainGradient)
        }
        text("PLAYTIME") {
            style {
                this.fill = Color.WHITE
                fontSize = 150.px
                fontWeight = FontWeight.EXTRA_BOLD
            }
        }
        button("Load Video") {
            addClass(Styles.loadVideoButton)
            action {
                val result = fileLoaderController.loadVideoFile()
                if(result) {
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
