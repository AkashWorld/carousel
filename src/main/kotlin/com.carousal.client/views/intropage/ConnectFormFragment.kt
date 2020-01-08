package com.carousal.client.views.intropage

import com.carousal.client.controllers.ConnectController
import com.carousal.client.views.ViewUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSpinner
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import tornadofx.*

class ConnectFormFragment : Fragment() {
    private val connectController: ConnectController by inject()
    private val usernameErrorMessage = SimpleStringProperty("")
    private lateinit var form: VBox

    override val root = stackpane {
        form = vbox {
            alignment = Pos.CENTER
            spacing = 50.0
            addClass(IntroPageStyles.rightFormPanel)
            val iconImage = Image(this::class.java.classLoader.getResourceAsStream("icons/CarousalIcon128.png"))
            imageview(iconImage)
            text("Connect to a friend's server!") {
                addClass(IntroPageStyles.formTitle)
            }
            textfield(connectController.serverAddressProperty) {
                addClass(IntroPageStyles.introPageTextFields)
                promptText = "Server Address"
            }
            passwordfield(connectController.passwordProperty) {
                addClass(IntroPageStyles.introPageTextFields)
                promptText = "Server Password"
            }
            vbox {
                alignment = Pos.CENTER
                textfield(connectController.usernameProperty) {
                    addClass(IntroPageStyles.introPageTextFields)
                    promptText = "Username"
                    this.setOnKeyReleased {
                        if (!connectController.validateUsername(connectController.usernameProperty.value)
                            && connectController.usernameProperty.value != ""
                        ) {
                            usernameErrorMessage.value = "Only alphanumeric characters are allowed"
                        } else if (connectController.usernameProperty.value.length > 25) {
                            usernameErrorMessage.value = "Username must be less than 25 characters"
                        } else {
                            usernameErrorMessage.value = ""
                        }
                    }
                }
                text(usernameErrorMessage) {
                    addClass(IntroPageStyles.errorMessage)
                }
            }
            val connectButton = JFXButton("Connect")
            connectButton.addClass(IntroPageStyles.formButton)
            connectButton.setOnAction {
                if (connectController.serverAddressProperty.value.isNullOrEmpty()) {
                    ViewUtils.showErrorDialog(
                        "Server Address must not be empty!",
                        primaryStage.scene.root as StackPane
                    )
                } else if (connectController.usernameProperty.value.isNullOrEmpty()) {
                    ViewUtils.showErrorDialog(
                        "Username must not be empty!",
                        primaryStage.scene.root as StackPane
                    )
                } else if (connectController.usernameProperty.value.length >= 25) {
                    ViewUtils.showErrorDialog(
                        "Username length must be less than 25",
                        primaryStage.scene.root as StackPane
                    )
                } else if (!connectController.validateUsername(connectController.usernameProperty.value)) {
                    ViewUtils.showErrorDialog(
                        "Only alphanumeric characters are allowed for the username",
                        primaryStage.scene.root as StackPane
                    )
                } else {
                    connectToServer()
                }
            }
            this.add(connectButton)
        }
    }

    private fun getSpinnerNode(): Pane {
        val container = VBox()
        container.alignment = Pos.CENTER
        container.addClass(IntroPageStyles.rightFormPanel)
        val spinner = JFXSpinner()
        spinner.addClass(IntroPageStyles.progressSpinner)
        container.add(spinner)
        return container
    }

    private fun showConnectForm() {
        root.children.clear()
        root.add(form)
    }

    private fun connectToServer() {
        root.children.clear()
        root.add(getSpinnerNode())
        connectController.signInRequest({
            showConnectForm()
            find<IntroPage>().transitionToPlayerPage()
        }, {
            showConnectForm()
            it?.run { ViewUtils.showErrorDialog(it, primaryStage.scene.root as StackPane) }
        })
    }

    override fun onUndock() {
        super.onUndock()
        showConnectForm()
    }
}