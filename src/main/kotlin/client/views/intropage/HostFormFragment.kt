package client.views.intropage

import client.controllers.HostController
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

class HostFormFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val hostController: HostController by inject()
    private val usernameErrorMessage = SimpleStringProperty("")
    private lateinit var form: VBox

    override val root = stackpane {
        addClass(IntroPageStyles.rightFormPanel)
        form = vbox {
            alignment = Pos.CENTER
            spacing = 50.0
            imageview(this::class.java.classLoader.getResource("icons/CarousalIcon256.png")?.toString())
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
                    showErrorDialog("Username must not be empty")
                } else if (!hostController.validateUsername(hostController.usernameProperty.value)) {
                    showErrorDialog("Username must contain only alphanumeric characters")
                } else if (hostController.usernameProperty.value.length >= 25) {
                    showErrorDialog("Username must contain less that 25 characters")
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

    private fun showErrorDialog(message: String?) {
        if (message == null) {
            return
        }
        showHostForm()
        val layout = JFXDialogLayout()
        val dialog = JFXDialog(primaryStage.scene.root as StackPane, layout, JFXDialog.DialogTransition.CENTER, true)
        layout.setHeading(Text("Error"))
        layout.setBody(Text(message))
        val closeButton = JFXButton("Okay")
        closeButton.setOnAction { dialog.close() }
        layout.setActions(closeButton)
        dialog.show()
    }

    private fun hostNewServer() {
        root.children.clear()
        root.add(getSpinnerNode())
        hostController.hostNewServer({ find<IntroPage>().transitionToPlayerPage() }, { showErrorDialog(it) })
    }
}