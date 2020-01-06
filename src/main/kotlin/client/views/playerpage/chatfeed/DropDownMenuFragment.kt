package client.views.playerpage.chatfeed

import client.controllers.ChatController
import client.models.ClientContextImpl
import client.views.playerpage.PlayerPage
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Side
import javafx.scene.paint.Color
import tornadofx.*

class DropDownMenuFragment : Fragment() {
    private val playerPage: PlayerPage by inject()
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
                    playerPage.navigateToFileLoader()
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
                    playerPage.navigateToIntroPage()
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