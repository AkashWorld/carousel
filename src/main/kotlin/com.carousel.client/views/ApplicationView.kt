package com.carousel.client.views

import com.carousel.client.models.ClientContextImpl
import com.carousel.client.views.intropage.IntroPage
import javafx.scene.image.Image
import javafx.stage.Screen
import com.carousel.server.Server
import javafx.application.Platform
import tornadofx.*
import kotlin.system.exitProcess

class ApplicationView : View() {
    companion object {
        const val APPLICATION_NAME = "Carousel"
    }

    init {
        Styles.getIconInputStream()?.run {
            addStageIcon(Image(this, 0.0, 0.0, true, true))
        }
        primaryStage.title = APPLICATION_NAME
        val screenBounds = Screen.getPrimary().visualBounds
        primaryStage.width = screenBounds.width * .80
        primaryStage.height = screenBounds.height * .80
        primaryStage.setOnCloseRequest {
            ClientContextImpl.getInstance().sendSignOutRequest()
            Server.getInstance().close()
            Platform.exit()
            exitProcess(0)
        }
    }

    override val root = stackpane {
        this.add(find<IntroPage>())
    }
}