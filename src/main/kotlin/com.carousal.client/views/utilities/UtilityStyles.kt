package com.carousal.client.views.utilities

import javafx.geometry.Pos
import javafx.geometry.VPos
import javafx.scene.Cursor
import javafx.scene.paint.Color
import tornadofx.*

class UtilityStyles : Stylesheet() {
    companion object {
        val notificationTabContainer by cssclass()
        val closeButton by cssclass()
        val notificationMessage by cssclass()
        val messageTextFlow by cssclass()
    }

    init {
        notificationTabContainer {
            maxWidth = 350.px
            backgroundColor = multi(Color.WHITE)
            backgroundRadius = multi(box(10.px))
            borderRadius = multi(box(10.px))
            padding = box(15.px)
            alignment = Pos.CENTER
            and(hover) {
                cursor = Cursor.HAND
            }
        }
        closeButton {
            backgroundColor = multi(Color.TRANSPARENT)
            padding = box(0.px)
        }
        messageTextFlow {
            wrapText = true
            vAlignment = VPos.CENTER
        }
        notificationMessage {
            fill = Color.BLACK
            fontSize = 15.px
        }
    }

}