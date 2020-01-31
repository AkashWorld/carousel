package com.carousel.client.views.playerpage.chatfeed

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*

class GiphyImageButton : Fragment() {
    override val root = button {
        addClass(ChatFeedStyles.emojiButton)
        val icon = MaterialIconView(MaterialIcon.IMAGE, "30px")
        icon.fill = ChatFeedStyles.chatTextColor
        icon.onHover {
            if (it) {
                icon.fill = Color.DARKGRAY
            } else {
                icon.fill = ChatFeedStyles.chatTextColor
            }
        }
        setOnMouseClicked {
        }
        tooltip("Browse GIPHY") {
            showDelay = Duration.ZERO
        }
        this.add(icon)
    }
}