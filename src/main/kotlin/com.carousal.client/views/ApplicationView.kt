package com.carousal.client.views

import com.carousal.client.models.ClientContextImpl
import com.carousal.client.views.intropage.IntroPage
import javafx.scene.image.Image
import javafx.stage.Screen
import com.carousal.server.Server
import tornadofx.*

class ApplicationView : View() {
    companion object {
        const val APPLICATION_NAME = "Carousal"
    }

    init {
        Styles.getIconPath()?.run {
            addStageIcon(Image(this, 0.0, 0.0, true, true))
        }
        primaryStage.title = APPLICATION_NAME
        val screenBounds = Screen.getPrimary().visualBounds
        primaryStage.width = screenBounds.width * .80
        primaryStage.height = screenBounds.height * .80
        primaryStage.setOnCloseRequest {
            ClientContextImpl.getInstance().sendSignOutRequest()
            Server.getInstance().close()
        }
    }

    override val root = stackpane {
        this.add(find<IntroPage>())
    }
}