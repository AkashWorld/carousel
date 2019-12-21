package client.playerpage

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import tornadofx.*


class FileLoaderView: View() {
    private val fileLoaderController: FileLoaderController by inject()
    private var container: VBox? = null
    private var loadingErrorMessage: SimpleStringProperty = SimpleStringProperty("")

    override val root = vbox {
        container = this
        prefWidth = 1600.0
        prefHeight = 1200.0
        alignment = Pos.CENTER
        style {
            this.backgroundColor = multi(mainGradient)
        }
        text("PLAYTIME") {
            style {
                this.fill = Color.WHITE
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
                val result = fileLoaderController.loadVideoFile()
                if(result) {
                    replaceWith<MediaPlayerView>()
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
