package client.controllers

import client.views.playerpage.chatfeed.ImageLoader
import client.views.playerpage.chatfeed.ImageLoaderImpl
import javafx.scene.image.Image
import javafx.scene.shape.Rectangle
import javafx.stage.FileChooser
import tornadofx.*
import java.io.File

class ImageLoaderController : Controller() {
    private val imageBounds = Rectangle(340.0, 340.0)
    private val chatController: ChatController by inject()
    private val imageLoader: ImageLoader = ImageLoaderImpl()
    private val photoFilter =
        FileChooser.ExtensionFilter("Photos", "*.jpg", "*.jpeg", "*.png", "*.gif")

    fun loadImage(error: () -> Unit) {
        val returnedFiles = chooseFile("Choose image", arrayOf(photoFilter)) {
            val homeDir: String = System.getProperty("user.home")
            val imageDirPath = File("$homeDir/Pictures")
            this.initialDirectory = imageDirPath
        }
        runAsync {
            if (returnedFiles.isEmpty()) {
                return@runAsync
            } else if (returnedFiles.size != 1) {
                ui {
                    error()
                }
                return@runAsync
            }
            val file = returnedFiles.first()
            if (!file.isFile) {
                ui {
                    error()
                }
                return@runAsync
            }
            val imagePath = file.canonicalPath
            try {
                val encodedImage = imageLoader.getBase64EncodingOfImage(imagePath, imageBounds.width.toInt(), imageBounds.height.toInt())
                if (encodedImage == null) {
                    ui {
                        error()
                    }
                    return@runAsync
                }
                chatController.addImage(encodedImage) { ui { error() } }
            } catch (e: Exception) {
                ui {
                    error()
                }
                return@runAsync
            }
        }
    }

    fun getImageFromEncoding(encodedImage: String): Image? {
        return imageLoader.getImageFromBase64Encoding(encodedImage, imageBounds.width, imageBounds.height)
    }
}