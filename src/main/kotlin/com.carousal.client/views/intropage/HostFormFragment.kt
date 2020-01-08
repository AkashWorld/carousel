package com.carousal.client.views.intropage

import com.carousal.client.controllers.HostController
import com.carousal.client.views.ViewUtils
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXSpinner
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import org.slf4j.LoggerFactory
import tornadofx.*

class HostFormFragment : Fragment() {
    private val hostController: HostController by inject()
    private val usernameErrorMessage = SimpleStringProperty("")
    private lateinit var form: VBox

    override val root = stackpane {
        addClass(IntroPageStyles.rightFormPanel)
        form = vbox {
            alignment = Pos.CENTER
            spacing = 50.0
            imageview(this::class.java.classLoader.getResource("icons/CarousalIcon128.png")?.toString())
            text("Host a server for your friends!") {
                addClass(IntroPageStyles.formTitle)
            }
            vbox {
                alignment = Pos.CENTER
                textfield(hostController.usernameProperty) {
                    addClass(IntroPageStyles.introPageTextFields)
                    promptText = "Username"
                    this.setOnKeyReleased {
                        if (!hostController.validateUsername(hostController.usernameProperty.value)
                            && !hostController.usernameProperty.value.isNullOrEmpty()
                        ) {
                            usernameErrorMessage.value = "Only alphanumeric characters are allowed"
                        } else if (hostController.usernameProperty.value.length > 25) {
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
            passwordfield(hostController.passwordProperty) {
                addClass(IntroPageStyles.introPageTextFields)
                promptText = "New Server Password"
            }
            val hostButton = JFXButton("Host")
            hostButton.addClass(IntroPageStyles.formButton)
            hostButton.setOnAction {
                if (hostController.usernameProperty.value.isNullOrEmpty()) {
                    ViewUtils.showErrorDialog("Username must not be empty", primaryStage.scene.root as StackPane)
                } else if (!hostController.validateUsername(hostController.usernameProperty.value)) {
                    ViewUtils.showErrorDialog(
                        "Username must contain only alphanumeric characters",
                        primaryStage.scene.root as StackPane
                    )
                } else if (hostController.usernameProperty.value.length >= 25) {
                    ViewUtils.showErrorDialog(
                        "Username must contain less that 25 characters",
                        primaryStage.scene.root as StackPane
                    )
                } else {
                    hostNewServer()
                }
            }
            this.add(hostButton)
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

    private fun showHostForm() {
        root.children.clear()
        root.add(form)
    }

    private fun hostNewServer() {
        root.children.clear()
        root.add(getSpinnerNode())
        hostController.hostNewServer({
            showHostForm()
            find<IntroPage>().transitionToPlayerPage()
        }, {
            showHostForm()
            it?.run { ViewUtils.showErrorDialog(it, primaryStage.scene.root as StackPane) }
        })
    }

    override fun onUndock() {
        super.onUndock()
        showHostForm()
    }
}