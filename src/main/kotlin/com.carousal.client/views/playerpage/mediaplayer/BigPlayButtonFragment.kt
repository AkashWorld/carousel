package com.carousal.client.views.playerpage.mediaplayer

import animatefx.animation.*
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.scene.paint.Color
import tornadofx.*

class BigPlayButtonFragment : Fragment() {
    private val pauseIcon = MaterialIconView(MaterialIcon.PAUSE, "150px")
    private val playIcon = MaterialIconView(MaterialIcon.PLAY_ARROW, "150px")
    private var currentAnimation: AnimationFX? = null
    private var secondAnimation: AnimationFX? = null
    override val root = button {
        addClass(MediaPlayerStyles.mediaBigPlayButton)
        isVisible = false
        isPickOnBounds = false
    }

    init {
        pauseIcon.fill = Color.LIGHTGRAY
        playIcon.fill = Color.LIGHTGRAY
    }

    fun triggerPlay() {
        root.getChildList()?.clear()
        root.add(playIcon)
        executeAnimation()
    }

    fun triggerPause() {
        root.getChildList()?.clear()
        root.add(pauseIcon)
        executeAnimation()
    }

    private fun executeAnimation() {
        currentAnimation?.stop()
        secondAnimation?.stop()
        currentAnimation = null
        secondAnimation = null
        val animation = ZoomIn(root)
        animation.setSpeed(2.0)
        animation.setOnFinished {
            secondAnimation = ZoomOut(root)
            secondAnimation?.setOnFinished {
                currentAnimation = animation
                root.isVisible = false
            }
            secondAnimation?.setSpeed(2.0)
            secondAnimation?.play()
        }
        root.isVisible = true
        animation.play()
    }
}