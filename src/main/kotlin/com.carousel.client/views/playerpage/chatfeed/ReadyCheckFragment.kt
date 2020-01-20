package com.carousel.client.views.playerpage.chatfeed

import com.carousel.client.controllers.UsersController
import com.carousel.client.views.utilities.ViewUtils
import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.util.Duration
import tornadofx.*

class ReadyCheckFragment : Fragment() {
    private val usersController: UsersController by inject()

    override val root = button {
        addClass(ChatFeedStyles.emojiButton)
        val icon = MaterialIconView(MaterialIcon.THUMBS_UP_DOWN, "30px")
        icon.fill = ChatFeedStyles.chatTextColor
        icon.onHover {
            if (it) {
                icon.fill = Color.DARKGRAY
            } else {
                icon.fill = ChatFeedStyles.chatTextColor
            }
        }
        setOnMouseClicked {
            usersController.sendInitiateReadyCheck({}, {
                ViewUtils.showErrorDialog("Could not initiate ready check.", primaryStage.scene.root as StackPane)
            })
        }
        tooltip("Initiate Ready Check") {
            showDelay = Duration.ZERO
        }
        this.add(icon)
    }
}