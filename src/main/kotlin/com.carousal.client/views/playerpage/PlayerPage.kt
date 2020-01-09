package com.carousal.client.views.playerpage

import com.carousal.client.controllers.ChatController
import com.carousal.client.controllers.UsersController
import com.carousal.client.models.*
import com.carousal.client.views.ViewUtils
import com.carousal.client.views.intropage.IntroPage
import com.carousal.client.views.playerpage.chatfeed.ChatFragment
import com.carousal.client.views.playerpage.fileloader.FileLoaderView
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import javafx.beans.value.ChangeListener
import javafx.scene.layout.StackPane
import javafx.scene.text.Text
import tornadofx.*

class PlayerPage : View() {

    private val introPageView: IntroPage by inject()
    private val fileLoaderView: FileLoaderView by inject()
    private val chatController: ChatController by inject()
    private val usersController: UsersController by inject()
    private var isReadyCheckOngoing = false
    private val readyCheckListener: ChangeListener<UserActionEvent?> = ChangeListener { _, _, newValue ->
        newValue?.run {
            if (newValue.action == UserAction.READY_CHECK && !isReadyCheckOngoing) {
                showReadyCheckDialog()
            }
        }
    }

    override val root = borderpane {
        setMinSize(0.0, 0.0)
        center {
            this.add(fileLoaderView)
        }
        right {
            chatController.isChatShown().addListener { _, _, newValue ->
                if (!newValue) {
                    this.children.remove(this.right)
                } else {
                    this.right = find<ChatFragment>().root
                }
            }
        }
    }

    fun navigateToIntroPage() {
        if (isDocked) {
            root.replaceWith(introPageView.root, ViewTransition.Fade(1000.millis))
        }
    }

    fun navigateToFileLoader() {
        if (isDocked && root.center != fileLoaderView.root) {
            root.center = fileLoaderView.root
        }
    }

    private fun showReadyCheckDialog() {
        val readyCheckDialog: JFXDialog
        val layout = JFXDialogLayout()
        readyCheckDialog =
            JFXDialog(primaryStage.scene.root as StackPane, layout, JFXDialog.DialogTransition.CENTER, false)
        layout.setHeading(Text("Ready Check"))
        layout.setBody(Text("Are you ready?"))
        val yesButton = JFXButton("Yes")
        yesButton.setOnAction {
            usersController.sendIsReady(
                true,
                { readyCheckDialog.close() },
                { readyCheckDialog.close() })
        }
        val noButton = JFXButton("No")
        noButton.setOnAction {
            usersController.sendIsReady(
                false,
                { readyCheckDialog.close() },
                { readyCheckDialog.close() })
        }
        layout.setActions(yesButton, noButton)
        readyCheckDialog.setOnDialogOpened { isReadyCheckOngoing = true }
        readyCheckDialog.setOnDialogClosed { isReadyCheckOngoing = false }
        runLater(30000.millis) {
            if (isReadyCheckOngoing) {
                usersController.sendIsReady(false, {}, {})
                readyCheckDialog.close()
            }
        }
        readyCheckDialog.show()
    }

    override fun onDock() {
        super.onDock()
        root.right = find<ChatFragment>().root
        chatController.subscribeToMessages {
            ViewUtils.showErrorDialog(
                "Connection with chat is lost, please restart this application.",
                primaryStage.scene.root as StackPane
            )
        }
        usersController.getUserActionObservable().addListener(readyCheckListener)
    }

    override fun onUndock() {
        super.onUndock()
        usersController.getUserActionObservable().removeListener(readyCheckListener)
        chatController.cleanUp()
        root.center = fileLoaderView.root
        root.children.remove(root.right)
        ClientContextImpl.getInstance().clearContext()
    }
}
