package client.intropage

import client.Styles
import javafx.scene.Cursor
import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class IntroPageStyles : Stylesheet() {
    companion object {
        val leftIntroPanel by cssclass()
        val rightFormPanel by cssclass()
        val titleText by cssclass()
        val subText by cssclass()
        val introPageTextFields by cssclass()
        val formButton by cssclass()
        val formTitle by cssclass()
        val errorMessage by cssclass()
        val progressSpinner by cssclass()
    }

    init {
        val backgroundLocation = Styles.getRandomBackground()
        leftIntroPanel {
            backgroundImage = multi(backgroundLocation!!)
            backgroundPosition = multi(BackgroundPosition.CENTER)
            backgroundRepeat = multi(Pair(BackgroundRepeat.SPACE, BackgroundRepeat.SPACE))
            backgroundSize = multi(
                BackgroundSize(
                    100.0, 100.0,
                    true, true, false, true
                )
            )
        }
        titleText {
            fontSize = 200.px
            fill = Color.WHITE
            fontWeight = FontWeight.EXTRA_BOLD
        }
        subText {
            fontSize = 20.px
            fill = Color.WHITE
        }
        toggleButton {
            fontSize = 20.px
            fontWeight = FontWeight.MEDIUM
            backgroundRadius = multi(box(50.px))
            prefWidth = 300.px
            prefHeight = 60.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(Styles.buttonColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(Styles.lightButtonColor)
                cursor = Cursor.HAND
            }
            and(selected) {
                backgroundColor = multi(Styles.darkButtonColor)
                backgroundRadius = multi(box(50.px))
                borderRadius = multi(box(50.px))
                borderWidth = multi(box(5.px))
                borderColor = multi(box(Styles.lightButtonColor))
            }
        }
        rightFormPanel {
            backgroundColor = multi(Styles.mainGradient)
            prefWidth = 370.px
        }
        introPageTextFields {
            fontSize = 15.px
            maxWidth = 275.px
            prefHeight = 40.px
            backgroundRadius = multi(box(50.px))
            borderRadius = multi(box(50.px))
            accentColor = Styles.lightButtonColor
            and(focused) {
                borderWidth = multi(box(5.px))
                borderColor = multi(box(Styles.lightButtonColor))
            }
        }
        formButton {
            fontSize = 20.px
            fontWeight = FontWeight.MEDIUM
            backgroundRadius = multi(box(50.px))
            prefWidth = 150.px
            prefHeight = 30.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(Styles.buttonColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(Styles.lightButtonColor)
                cursor = Cursor.HAND
            }
        }
        formTitle {
            fontSize = 20.px
            fill = Color.WHITE
            fontWeight = FontWeight.EXTRA_BOLD
        }
        errorMessage {
            fontSize = 15.px
            fill = Color.WHITE
        }
        progressSpinner {
            progressColor = Styles.lightButtonColor
        }
    }
}