package client

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*


class Styles : Stylesheet() {
    companion object {
        val chatButton by cssclass()
        val loadVideoButton by cssclass()
        val chatTextField by cssclass()

        val buttonColor = Color.valueOf("#661894")
        val hoverButtonColor = Color.valueOf("#370d4f")
        val chatColor = Color.valueOf("#262626")
        val chatBackgroundColor = Color.valueOf("#171717")
    }

    init {
        Stylesheet.listView {
            Stylesheet.scrollBar {
                backgroundColor = multi(chatColor)
                and(vertical) {
                    prefWidth = 10.px
                    Stylesheet.thumb {
                        backgroundColor = multi(Color.valueOf("#424242"))
                        backgroundRadius = multi(box(50.px))
                    }
                    Stylesheet.incrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                    Stylesheet.decrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                }
            }
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
            }
        }
        chatButton {
            prefWidth = 50.px
            prefHeight = 30.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(buttonColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(hoverButtonColor)
            }
        }
        chatTextField {
            backgroundColor = multi(Color.valueOf("#303030"))
            textFill = Color.WHITE
            minHeight = 45.px
            prefWidth = 310.px
            padding = box(10.px)
            and(focused) {
                borderRadius = multi(box(3.px))
                borderWidth = multi(box(3.px))
                borderColor = multi(box(Color.PURPLE))
            }
        }
    }
}
