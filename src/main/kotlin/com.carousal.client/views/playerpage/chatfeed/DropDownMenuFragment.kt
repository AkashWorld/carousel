package com.carousal.client.views.playerpage.chatfeed

import com.carousal.client.controllers.ChatController
import com.carousal.client.controllers.PlayerPageController
import com.carousal.client.models.ClientContextImpl
import com.carousal.client.views.playerpage.PlayerPage
import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Side
import javafx.scene.paint.Color
import tornadofx.*

class DropDownMenuFragment : Fragment() {
    private val playerPageController: PlayerPageController by inject()
    private val chatController : ChatController by inject()

    override val root = button {
        addClass(ChatFeedStyles.emojiButton)
        val icon = MaterialIconView(MaterialIcon.MENU, "30px")
        icon.fill = ChatFeedStyles.chatTextColor
        icon.onHover {
            if (it) {
                icon.fill = Color.DARKGRAY
            } else {
                icon.fill = ChatFeedStyles.chatTextColor
            }
        }
        val menu = contextmenu {
            isAutoHide = true
            item("Load View") {
                setOnAction {
                    playerPageController.navigateToFileLoaderPage()
                }
            }
            item("Toggle Status Info") {
                setOnAction {
                    chatController.toggleIsInfoShown()
                }
            }
            item("Exit") {
                setOnAction {
                    ClientContextImpl.getInstance().sendSignOutRequest()
                    playerPageController.exitToIntroPage()
                }
            }
        }
        this.add(icon)
        setOnAction {
            if (menu.isShowing) {
                menu.hide()
            } else {
                menu.show(this, Side.LEFT, 0.0, 0.0)
            }
        }
    }
}