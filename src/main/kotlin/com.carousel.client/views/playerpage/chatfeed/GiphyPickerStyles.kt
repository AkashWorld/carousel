package com.carousel.client.views.playerpage.chatfeed

import javafx.scene.Cursor
import javafx.scene.paint.Color
import tornadofx.*

class GiphyPickerStyles : Stylesheet() {
    companion object {
        val giphyPickerContainer by cssclass()
        val giphyTextField by cssclass()
        val gifListView by cssclass()
        val gifBackgroundColor = Color.valueOf("#171717")!!
        val gifIVStyle by cssclass()
    }

    init {
        gifListView {
            listCell {
                and(even) {
                    backgroundColor = multi(gifBackgroundColor)
                }
                and(odd) {
                    backgroundColor = multi(gifBackgroundColor)
                }
            }
        }
        giphyPickerContainer {
            prefWidth = 275.px
            prefHeight = 400.px
            backgroundColor = multi(gifBackgroundColor)
            backgroundRadius = multi(box(10.px))
            borderRadius = multi(box(10.px))
            borderColor = multi(box(Color(.27, .27, .27, 1.0)))
        }
        giphyTextField {
            backgroundColor = multi(Color.valueOf("#303030"))
            borderRadius = multi(box(50.px))
            backgroundRadius = multi(box(50.px))
            fontSize = 15.px
            textFill = Color.WHITE
            prefHeight = 20.px
            prefWidth = Int.MAX_VALUE.px
            backgroundInsets = multi(box(5.px))
        }
        gifIVStyle {
            and(hover) {
                cursor = Cursor.HAND
            }
        }
    }
}