package com.carousel.client.views.playerpage.chatfeed

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import javafx.util.Duration
import tornadofx.*

class GiphyImageButton : Fragment() {
    private val giphyPicker: GiphyPicker = find()

    override val root = button {
        addClass(ChatFeedStyles.emojiButton)
        val icon = MaterialIconView(MaterialIcon.WALLPAPER, "30px")
        icon.fill = ChatFeedStyles.chatTextColor
        icon.onHover {
            if (it) {
                icon.fill = Color.DARKGRAY
            } else {
                icon.fill = ChatFeedStyles.chatTextColor
            }
        }
        setOnMouseClicked {
            val giphyStage = giphyPicker.openWindow(StageStyle.TRANSPARENT)
            giphyStage?.isAlwaysOnTop = true
            giphyStage?.x = it.screenX
            giphyStage?.y = it.screenY - 485.0
            primaryStage.scene.setOnMouseClicked {
                giphyStage?.close()
            }
        }
        tooltip("Browse GIPHY") {
            showDelay = Duration.ZERO
        }
        this.add(icon)
    }
}