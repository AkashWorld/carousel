package com.carousel.client.views.intropage

import com.carousel.client.views.ApplicationView
import com.carousel.client.models.ClientContextImpl
import com.carousel.client.views.Styles
import com.carousel.client.views.playerpage.PlayerPage
import javafx.geometry.Pos
import javafx.scene.control.ToggleGroup
import com.carousel.server.Server
import javafx.scene.image.Image
import javafx.scene.layout.*
import tornadofx.*

class IntroPage : View() {
    private val hostForm = find<HostFormFragment>()
    private val connectForm = find<ConnectFormFragment>()
    private lateinit var toggleGroup: ToggleGroup

    override val root = borderpane {
        center {
            vbox {
                alignment = Pos.CENTER
                spacing = 30.0
                val backgroundImage = BackgroundImage(
                    Image(Styles.getRandomBackground()),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    BackgroundSize(100.0, 100.0, true, true, false, true)
                )
                background = Background(backgroundImage)

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
                if (newValue == null) {
                    return@addListener
                }
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
        root.replaceWith(find<PlayerPage>().root)
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