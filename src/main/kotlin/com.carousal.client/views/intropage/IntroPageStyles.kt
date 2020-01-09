package com.carousal.client.views.intropage

import com.carousal.client.views.Styles
import javafx.scene.Cursor
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*

class IntroPageStyles : Stylesheet() {
    companion object {
        val rightFormPanel by cssclass()
        val titleText by cssclass()
        val subText by cssclass()
        val introPageTextFields by cssclass()
        val formButton by cssclass()
        val formTitle by cssclass()
        val errorMessage by cssclass()
        val progressSpinner by cssclass()
        val jfxCheckedColor by cssproperty<Color>("-jfx-checked-color")
        val jfxUncheckedColor by cssproperty<Color>("-jfx-unchecked-color")
        val jfxCheckBox by cssclass()
        val arc by cssclass()
    }

    init {
        titleText {
            fontSize = 150.px
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
            backgroundColor = multi(Styles.defaultColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(Styles.lightColor)
                cursor = Cursor.HAND
            }
            and(selected) {
                backgroundColor = multi(Styles.darkColor)
                backgroundRadius = multi(box(50.px))
                borderRadius = multi(box(50.px))
                borderWidth = multi(box(5.px))
                borderColor = multi(box(Styles.lightColor))
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
            accentColor = Styles.lightColor
            and(focused) {
                borderWidth = multi(box(5.px))
                borderColor = multi(box(Styles.lightColor))
            }
        }
        formButton {
            fontSize = 20.px
            fontWeight = FontWeight.MEDIUM
            backgroundRadius = multi(box(50.px))
            prefWidth = 150.px
            prefHeight = 30.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(Styles.defaultColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(Styles.lightColor)
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
            arc {
                stroke = Styles.lightColor
            }
        }
        jfxCheckBox {
            jfxCheckedColor.value = Styles.lightColor
            jfxUncheckedColor.value = Styles.darkColor
            text {
                fill = Color.WHITE
            }
        }
    }
}