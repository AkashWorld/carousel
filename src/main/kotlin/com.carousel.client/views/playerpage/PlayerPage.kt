package com.carousel.client.views.playerpage

import com.carousel.client.controllers.ChatController
import com.carousel.client.controllers.NotificationController
import com.carousel.client.controllers.UsersController
import com.carousel.client.models.observables.Notification
import com.carousel.client.views.utilities.ViewUtils
import com.carousel.client.views.intropage.IntroPage
import com.carousel.client.models.ClientContextImpl
import com.carousel.client.models.UserAction
import com.carousel.client.models.UserActionEvent
import com.carousel.client.views.playerpage.chatfeed.ChatFragment
import com.carousel.client.views.playerpage.fileloader.FileLoaderView
import com.carousel.client.views.utilities.NotificationTabFragment
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
    private val notificationController: NotificationController by inject()
    private var isReadyCheckOngoing = false
    private val readyCheckListener: ChangeListener<UserActionEvent?> = ChangeListener { _, _, newValue ->
        newValue?.run {
            if (newValue.action == UserAction.READY_CHECK && !isReadyCheckOngoing) {
                showReadyCheckDialog()
            }
        }
    }
    private val notificationListener: ChangeListener<Notification?> = ChangeListener { _, _, newValue ->
        newValue?.run {
            primaryStage.scene.root.add(find<NotificationTabFragment>(mapOf("message" to this.content)))
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
        notificationController.sendNotificationSubscription {
            ViewUtils.showErrorDialog(
                "Connection is lost, please restart this application.",
                primaryStage.scene.root as StackPane
            )
        }
        usersController.getUserActionObservable().addListener(readyCheckListener)
        notificationController.getNotificationListener().addListener(notificationListener)
    }

    override fun onUndock() {
        super.onUndock()
        usersController.getUserActionObservable().removeListener(readyCheckListener)
        notificationController.getNotificationListener().removeListener(notificationListener)
        chatController.cleanUp()
        root.center = fileLoaderView.root
        root.children.remove(root.right)
        ClientContextImpl.getInstance().clearContext()
    }
}
