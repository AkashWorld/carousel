package client

import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import tornadofx.*


class Styles : Stylesheet() {
    companion object {
        val chatButton by cssclass()
        val loadVideoButton by cssclass()
        val chatTextField by cssclass()
        val emojiButton by cssclass()
        val emojiPickerContainer by cssclass()
        val emojiTextField by cssclass()

        val buttonColor = Color.valueOf("#661894")
        val hoverButtonColor = Color.valueOf("#370d4f")
        val chatColor = Color.valueOf("#262626")
        val chatBackgroundColor = Color.valueOf("#171717")
        val chatTextColor = Color.valueOf("#e3e3e3")
        val chatFontSize = 14.px
    }

    init {
        listView {
            scrollBar {
                backgroundColor = multi(chatColor)
                and(vertical) {
                    prefWidth = 10.px
                    thumb {
                        backgroundColor = multi(Color.valueOf("#424242"))
                        backgroundRadius = multi(box(50.px))
                    }
                    incrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                    decrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                }
            }
        }
        scrollPane {
            scrollBar {
                backgroundColor = multi(chatColor)
                and(vertical) {
                    prefWidth = 10.px
                    thumb {
                        backgroundColor = multi(Color.valueOf("#424242"))
                        backgroundRadius = multi(box(50.px))
                    }
                    incrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                    decrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                }
                and(horizontal) {
                    backgroundColor = multi(Color.TRANSPARENT)
                    incrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                    decrementButton {
                        backgroundColor = multi(Color.TRANSPARENT)
                    }
                    thumb {
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
            padding = box(10.px)
            and(focused) {
                borderRadius = multi(box(3.px))
                borderWidth = multi(box(3.px))
                borderColor = multi(box(Color.PURPLE))
            }
        }
        emojiButton {
            backgroundColor = multi(Color.TRANSPARENT)
            maxHeight = 30.px
            padding = box(0.px)
        }
        emojiPickerContainer {
            prefWidth = 275.px
            prefHeight = 400.px
            backgroundColor = multi(chatBackgroundColor)
            backgroundRadius = multi(box(10.px))
            borderRadius = multi(box(10.px))
            borderColor = multi(box(Color(.27, .27, .27, 1.0)))
        }
        emojiTextField {
            backgroundColor = multi(Color.valueOf("#303030"))
            borderRadius = multi(box(50.px))
            backgroundRadius = multi(box(50.px))
            fontSize = 15.px
            textFill = Color.WHITE
            prefHeight = 20.px
            prefWidth = Int.MAX_VALUE.px
            backgroundInsets = multi(box(5.px))
        }
    }
}
