package client.views.playerpage.chatfeed

import client.views.playerpage.PlayerPage
import com.jfoenix.controls.JFXListView
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.geometry.Side
import javafx.scene.control.MenuItem
import javafx.scene.control.SelectionMode
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import tornadofx.*

class DropDownMenuFragment : Fragment() {
    private val playerPage: PlayerPage by inject()

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
            item("Exit") {
                setOnAction {
                    playerPage.navigateToIntroPage()
                }
            }
        }
        this.add(icon)
        setOnAction {
            if (menu.isShowing) {
                menu.hide()
            } else {
                menu.show(this, Side.TOP, 0.0, 0.0)
            }
        }
    }
}