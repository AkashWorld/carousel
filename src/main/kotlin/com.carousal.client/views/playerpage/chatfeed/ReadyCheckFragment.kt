package com.carousal.client.views.playerpage.chatfeed

import com.carousal.client.controllers.UsersController
import com.carousal.client.views.ViewUtils
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.StageStyle
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