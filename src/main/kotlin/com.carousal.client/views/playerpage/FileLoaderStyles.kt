package com.carousal.client.views.playerpage

import com.carousal.client.views.Styles
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.text.FontWeight
import tornadofx.*

class FileLoaderStyles : Stylesheet() {
    companion object {
        val loadVideoButton by cssclass()
        val titleText by cssclass()

        val buttonColor = Styles.defaultColor
        val hoverButtonColor = Styles.lightColor
        val mainGradient: LinearGradient = Styles.mainGradient
    }

    init {
        titleText {
            fontSize = 200.px
            fill = Color.WHITE
            fontWeight = FontWeight.EXTRA_BOLD
        }
        loadVideoButton {
            fontSize = 20.px
            fontWeight = FontWeight.MEDIUM
            backgroundRadius = multi(box(50.px))
            prefWidth = 300.px
            prefHeight = 60.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(buttonColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(hoverButtonColor)
                cursor = Cursor.HAND
            }
        }
    }
}