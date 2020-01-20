package com.carousel.client.views.utilities

import animatefx.animation.FadeOut
import animatefx.animation.SlideInUp
import javafx.scene.layout.AnchorPane
import tornadofx.*

class NotificationTabFragment : Fragment() {
    private val message: String by param("")
    private lateinit var rootReference: AnchorPane

    override val root = anchorpane {
        rootReference = this
        isPickOnBounds = false
        stackpane {
            AnchorPane.setLeftAnchor(this, 25.0)
            AnchorPane.setBottomAnchor(this, 75.0)
            addClass(UtilityStyles.notificationTabContainer)
            textflow {
                addClass(UtilityStyles.messageTextFlow)
                text(message) {
                    addClass(UtilityStyles.notificationMessage)
                }
            }
            setOnMouseClicked {
                val outAnimation = FadeOut(rootReference)
                outAnimation.setOnFinished {
                    rootReference.removeFromParent()
                }
                outAnimation.play()
            }
            tooltip("Dismiss") {
                showDelay = 0.millis
            }
        }
    }

    override fun onDock() {
        super.onDock()
        val animation = SlideInUp(root)
        animation.setOnFinished {
            runLater(7500.millis) {
                val outAnimation = FadeOut(root)
                outAnimation.setOnFinished {
                    this.removeFromParent()
                }
                outAnimation.play()
            }
        }
        animation.play()
    }
}