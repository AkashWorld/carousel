package com.carousel.client.views.playerpage.chatfeed

import com.carousel.client.controllers.ImageLoaderController
import com.carousel.client.views.utilities.ViewUtils
import com.jfoenix.controls.JFXSpinner
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.control.Button
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*

class InsertImageButtonFragment : Fragment() {
    private val imageLoaderController: ImageLoaderController by inject()
    private lateinit var imageLoaderButton: Button

    override val root = stackpane {
        imageLoaderButton = button {
            addClass(ChatFeedStyles.emojiButton)
            val icon = MaterialIconView(MaterialIcon.IMAGE, "30px")
            icon.fill = ChatFeedStyles.chatTextColor
            icon.onHover {
                if (it) {
                    icon.fill = Color.DARKGRAY
                } else {
                    icon.fill = ChatFeedStyles.chatTextColor
                }
            }
            setOnMouseClicked {
                handleImageLoader()
            }
            tooltip("Upload Image") {
                showDelay = Duration.ZERO
            }
            this.add(icon)
        }
    }

    private fun reshowButton() {
        root.children.clear()
        root.add(imageLoaderButton)
    }

    private fun handleImageLoader() {
        root.children.clear()
        val spinner = JFXSpinner()
        spinner.addClass(ChatFeedStyles.imageSpinner)
        root.add(spinner)
        imageLoaderController.loadImage({ reshowButton() }, {
            ViewUtils.showErrorDialog(
                it,
                primaryStage.scene.root as StackPane
            )
            reshowButton()
        })
    }
}