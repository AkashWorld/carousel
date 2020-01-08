package client.views.playerpage.chatfeed

import client.views.Styles
import javafx.scene.Cursor
import javafx.scene.paint.Color
import tornadofx.*


class ChatFeedStyles : Stylesheet() {
    companion object {
        val chatButton by cssclass()
        val chatTextField by cssclass()
        val chatListView by cssclass()
        val emojiButton by cssclass()
        val emojiPickerContainer by cssclass()
        val emojiTextField by cssclass()
        val emojiPickerButton by cssclass()
        val menuListView by cssclass()
        val imageSpinner by cssclass()
        val arc by cssclass()

        val buttonColor = Styles.buttonColor
        val hoverButtonColor = Styles.lightButtonColor
        val chatColor = Color.valueOf("#262626")
        val chatBackgroundColor = Color.valueOf("#171717")
        val chatTextColor = Color.valueOf("#e3e3e3")
        val chatFontSize = 14.px
    }

    init {
        chatListView {
            listCell {
                and(even) {
                    backgroundColor = multi(chatBackgroundColor)
                }
                and(odd) {
                    backgroundColor = multi(chatBackgroundColor)
                }
            }
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
        chatButton {
            prefWidth = 50.px
            prefHeight = 30.px
            focusColor = Color.TRANSPARENT
            backgroundColor = multi(buttonColor)
            textFill = Color.WHITE
            and(hover) {
                backgroundColor = multi(hoverButtonColor)
                cursor = Cursor.HAND
            }
        }
        chatTextField {
            backgroundColor = multi(Color.valueOf("#303030"))
            textFill = Color.WHITE
            minHeight = 45.px
            padding = box(10.px)
            accentColor = Styles.lightButtonColor
            and(focused) {
                borderRadius = multi(box(3.px))
                borderWidth = multi(box(3.px))
                borderColor = multi(box(buttonColor))
            }
        }
        emojiButton {
            backgroundColor = multi(Color.TRANSPARENT)
            maxHeight = 30.px
            padding = box(0.px)
            and(hover) {
                cursor = Cursor.HAND
            }
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
        emojiPickerButton {
            and(hover) {
                cursor = Cursor.HAND
            }
        }
        menuListView {
            backgroundColor = multi(Color.WHITE)
        }
        imageSpinner {
            prefWidth = 25.px
            prefHeight = 25.px
            arc {
                stroke = Styles.buttonColor
            }
        }
    }
}
