package com.carousal.client.views.playerpage

import com.carousal.client.views.ApplicationView
import com.carousal.client.controllers.FileLoaderController
import com.carousal.client.views.ViewUtils
import com.carousal.client.views.playerpage.FileLoaderStyles.Companion.mainGradient
import com.carousal.client.views.playerpage.mediaplayer.MediaPlayerView
import javafx.geometry.Pos
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import tornadofx.*


class FileLoaderView : View() {
    private val fileLoaderController: FileLoaderController by inject()
    private var container: VBox? = null

    override val root = vbox {
        hgrow = Priority.ALWAYS
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
                fileLoaderController.loadVideoFile({
                    replaceWith<MediaPlayerView>(ViewTransition.Fade(1000.millis))
                }, {
                    ViewUtils.showErrorDialog(it ?: "Could not load video", primaryStage.scene.root as StackPane)
                })
            }
        }
    }
}
