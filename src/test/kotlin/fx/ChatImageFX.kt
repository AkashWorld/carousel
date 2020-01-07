package fx

import client.views.playerpage.chatfeed.ImageLoaderImpl
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.junit.jupiter.api.Test
import tornadofx.*
import java.io.File

class ChatImageFX {

    class Application : App(ImageWindow::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class ImageWindow: View() {
        val imageLoader = ImageLoaderImpl()
        override val root = vbox {
            val container = this
            prefWidth = 500.0
            prefHeight = 1000.0
            button("Load") {
                setOnAction {
                    val imageFile = chooseFile("Choose photo", arrayOf()) {
                        val homeDir: String = System.getProperty("user.home")
                        val videoFilePath = File("$homeDir/Pictures")
                        this.initialDirectory = videoFilePath
                    }.first().canonicalPath
                    val encoding = imageLoader.getBase64EncodingOfImage(imageFile, 285, 285)
                    val newImage: Image = imageLoader.getImageFromBase64Encoding(encoding!!, 285.0, 285.0) as Image
                    val iv = ImageView(newImage)
                    container.add(iv)
                }
            }
        }
    }

    @Test
    fun imageLoaderTest() {
        launch<Application>()
    }
}