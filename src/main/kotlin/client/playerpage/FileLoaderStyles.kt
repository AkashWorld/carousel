package client.playerpage

import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import javafx.scene.text.FontWeight
import tornadofx.*
import tornadofx.Stylesheet
import tornadofx.cssclass

class FileLoaderStyles : Stylesheet() {
    companion object {
        val loadVideoButton by cssclass()

        val buttonColor = Color.valueOf("#db4057")
        val hoverButtonColor = Color.valueOf("#ff4a65")
        val mainGradient: LinearGradient = LinearGradient.valueOf("from 0% 0% to 100% 100%, #7a2334, #3e091b")
    }

    init {
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