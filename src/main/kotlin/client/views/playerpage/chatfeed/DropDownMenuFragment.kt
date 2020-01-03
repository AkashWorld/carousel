package client.views.playerpage.chatfeed

import com.jfoenix.controls.JFXListView
import javafx.scene.control.SelectionMode
import tornadofx.Fragment
import tornadofx.addClass
import tornadofx.stackpane
import tornadofx.toObservable

class DropDownMenuFragment : Fragment() {
    private val options = listOf("Load Video", "Exit")
    override val root = stackpane {
        val list = JFXListView<String>()
        list.addClass(ChatFeedStyles.menuListView)
        list.selectionModel.selectionMode = SelectionMode.SINGLE
        list.items = options.toObservable()
        list.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            performSelectionAction(newValue)
        }
        this.add(list)
    }

    private fun performSelectionAction(selection: String) {
        if (selection == "Load Video") {

        } else if (selection == "Exit") {

        }
    }
}