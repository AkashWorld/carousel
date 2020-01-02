package client.views.intropage

import client.controllers.ConnectController
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXDialog
import com.jfoenix.controls.JFXDialogLayout
import com.jfoenix.controls.JFXSpinner
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import org.slf4j.LoggerFactory
import tornadofx.*

class ConnectFormFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val connectController: ConnectController by inject()
    private val usernameErrorMessage = SimpleStringProperty("")
    private lateinit var form: VBox

    override val root = stackpane {
        form = vbox {
            alignment = Pos.CENTER
            spacing = 50.0
            addClass(IntroPageStyles.rightFormPanel)
            imageview(this::class.java.classLoader.getResource("icons/CarousalIcon256.png")?.toString())
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
                    showErrorDialog("Server Address must not be empty!")
                } else if (connectController.usernameProperty.value.isNullOrEmpty()) {
                    showErrorDialog("Username must not be empty!")
                } else if (connectController.usernameProperty.value.length >= 25) {
                    showErrorDialog("Username length must be less than 25")
                } else if (!connectController.validateUsername(connectController.usernameProperty.value)) {
                    showErrorDialog("Only alphanumeric characters are allowed for the username")
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

    private fun showErrorDialog(message: String?) {
        if (message == null) {
            return
        }
        showConnectForm()
        val layout = JFXDialogLayout()
        val dialog = JFXDialog(primaryStage.scene.root as StackPane, layout, JFXDialog.DialogTransition.CENTER, true)
        layout.setHeading(Text("Error"))
        layout.setBody(Text(message))
        val closeButton = JFXButton("Okay")
        closeButton.setOnAction { dialog.close() }
        layout.setActions(closeButton)
        dialog.show()
    }

    private fun connectToServer() {
        root.children.clear()
        root.add(getSpinnerNode())
        connectController.signInRequest({ find<IntroPage>().transitionToPlayerPage() }, { showErrorDialog(it) })
    }
}