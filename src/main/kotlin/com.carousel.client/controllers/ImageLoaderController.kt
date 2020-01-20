package com.carousel.client.controllers

import com.carousel.client.views.playerpage.chatfeed.ImageLoader
import com.carousel.client.views.playerpage.chatfeed.ImageLoaderImpl
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

    fun loadImage(success: () -> Unit, error: (String) -> Unit) {
        val returnedFiles = chooseFile("Choose image", arrayOf(photoFilter)) {
            val homeDir: String = System.getProperty("user.home")
            val imageDirPath = File("$homeDir/Pictures")
            this.initialDirectory = imageDirPath
        }
        if (returnedFiles.isEmpty()) {
            success()
            return
        } else if (returnedFiles.size != 1) {
            error("Please choose only one image!")
            return
        }
        val file = returnedFiles.first()
        if (!file.isFile) {
            error("Could not open file")
            return
        }
        runAsync {
            val imagePath = file.canonicalPath
            try {
                val encodedImage = imageLoader.getBase64EncodingOfImage(
                    imagePath,
                    imageBounds.width.toInt(),
                    imageBounds.height.toInt()
                )
                if (encodedImage == null) {
                    ui {
                        error("Could not open and compress image")
                    }
                    return@runAsync
                } else if (encodedImage.length * 2 > 5242880L) {
                    ui {
                        error("Image file size is too large")
                    }
                    return@runAsync
                }
                chatController.addImage(encodedImage, { ui { success() } }, { ui { error("Could not send image") } })
            } catch (e: Exception) {
                ui {
                    error("Could not open and compress image")
                }
                return@runAsync
            }
        }
    }

    fun getImageFromEncoding(encodedImage: String): Image? {
        return imageLoader.getImageFromBase64Encoding(encodedImage, imageBounds.width, imageBounds.height)
    }
}