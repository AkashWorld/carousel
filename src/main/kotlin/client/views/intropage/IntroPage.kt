package client.views.intropage

import client.views.ApplicationView
import client.models.ClientContextImpl
import client.views.playerpage.PlayerPage
import javafx.geometry.Pos
import javafx.scene.control.ToggleGroup
import org.slf4j.LoggerFactory
import server.Server
import tornadofx.*

class IntroPage : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val hostForm = find<HostFormFragment>()
    private val connectForm = find<ConnectFormFragment>()
    private lateinit var toggleGroup: ToggleGroup

    override val root = borderpane {
        center {
            vbox {
                alignment = Pos.CENTER
                spacing = 30.0
                addClass(IntroPageStyles.leftIntroPanel)
                text(ApplicationView.APPLICATION_NAME.toUpperCase()) {
                    addClass(IntroPageStyles.titleText)
                }
                text("Would you like to connect to a friend's server or host your own?") {
                    addClass(IntroPageStyles.subText)
                }
                hbox {
                    alignment = Pos.CENTER
                    spacing = 30.0
                    toggleGroup = togglegroup {
                        togglebutton("Connect", this) {
                            userData = ToggleEnum.CONNECT_PAGE
                            action {
                                isSelected = true
                            }
                        }
                        togglebutton("Host", this) {
                            userData = ToggleEnum.HOST_PAGE
                            action {
                                isSelected = true
                            }
                        }
                    }
                }
            }
        }
        right {
            this.add(connectForm)
            toggleGroup.selectedToggleProperty().addListener { _, _, newValue ->
                if (newValue.userData == ToggleEnum.HOST_PAGE) {
                    if (this.children.first() != hostForm.root) {
                        this.children.remove(connectForm.root)
                        this.right {
                            this.add(hostForm)
                        }
                    }
                } else if (newValue.userData == ToggleEnum.CONNECT_PAGE) {
                    if (this.children.first() != connectForm.root) {
                        this.children.remove(hostForm.root)
                        this.right {
                            this.add(connectForm)
                        }
                    }
                }
            }
        }
    }

    fun transitionToPlayerPage() {
        root.replaceWith(find<PlayerPage>().root, ViewTransition.Fade(1000.millis))
    }

    override fun onDock() {
        super.onDock()
        ClientContextImpl.getInstance().clearContext()
        Server.clear()
    }
}

enum class ToggleEnum {
    HOST_PAGE,
    CONNECT_PAGE
}