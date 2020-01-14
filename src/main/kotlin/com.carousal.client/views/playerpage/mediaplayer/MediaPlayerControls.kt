package com.carousal.client.views.playerpage.mediaplayer

import com.carousal.client.controllers.ChatController
import com.jfoenix.controls.JFXSlider
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import tornadofx.*
import java.util.concurrent.TimeUnit

class MediaPlayerControls : Fragment() {
    private var onPlay: () -> Unit = {}
    private var onPause: () -> Unit = {}
    private var onChange: (Double) -> Unit = {}
    private var onVolumeChange: (Double) -> Unit = {}
    private val totalTime = SimpleStringProperty("")
    private val currentTime = SimpleStringProperty("0:00")
    private val chatController: ChatController by inject()
    private var duration: Long? = 0
    private var isPaused = false
    private lateinit var slider: JFXSlider
    private lateinit var volSlider: JFXSlider
    private var isSliderBeingDragged = false
    private var isOverlayButtonChecked = true
    private var isChatSideBarShown = true
    private lateinit var playPauseButton: Button
    override val root = borderpane {
        /**
         * Slider
         */
        center {
            vbox {
                slider = JFXSlider(0.0, 1.0, 0.0)
                slider.addClass(MediaPlayerStyles.trackSlider)
                this.add(slider)
                borderpane {
                    paddingTop = 2.5
                    left {
                        hbox {
                            paddingLeft = 5.0
                            spacing = 5.0
                            /**
                             * Play Pause
                             */
                            playPauseButton = button {
                                val btn = this
                                addClass(MediaPlayerStyles.mediaPlayerButton)
                                val initIcon = MaterialIconView(MaterialIcon.PAUSE, "30px")
                                initIcon.fill = Color.LIGHTGRAY
                                btn.onHover {
                                    if (it) {
                                        initIcon.fill = Color.WHITE
                                    } else {
                                        initIcon.fill = Color.LIGHTGRAY
                                    }
                                }
                                this.add(initIcon)
                                val sendPausePlayRequest = {
                                    if (isPaused) {
                                        onPlay()
                                    } else {
                                        onPause()
                                    }
                                }
                                action {
                                    sendPausePlayRequest()
                                }
                                this.addEventFilter(KeyEvent.KEY_PRESSED) {
                                    if (it.code == KeyCode.SPACE) {
                                        sendPausePlayRequest()
                                    }
                                }
                            }
                            hbox {
                                val volBox = this
                                spacing = 5.0
                                button {
                                    addClass(MediaPlayerStyles.mediaPlayerButton)
                                    val icon = MaterialIconView(MaterialIcon.VOLUME_UP, "28px")
                                    icon.fill = Color.LIGHTGRAY
                                    volSlider = JFXSlider(0.0, 100.0, 100.0)
                                    volSlider.orientation = Orientation.HORIZONTAL
                                    volSlider.addClass(MediaPlayerStyles.volumeSlider)
                                    volBox.onHover { isHover ->
                                        if (isHover) {
                                            icon.fill = Color.WHITE
                                            volBox.add(volSlider)
                                        } else {
                                            icon.fill = Color.LIGHTGRAY
                                            volBox.getChildList()?.removeIf { it == volSlider }
                                        }
                                    }
                                    this.add(icon)
                                }
                            }
                            hbox {
                                paddingTop = 4.0
                                paddingLeft = 5.0
                                label(currentTime) {
                                    textFill = Color.LIGHTGRAY
                                    style {
                                        fontSize = 14.px
                                    }
                                }
                                label(" / ") {
                                    textFill = Color.LIGHTGRAY
                                    style {
                                        fontSize = 14.px
                                    }
                                }
                                label(totalTime) {
                                    textFill = Color.LIGHTGRAY
                                    style {
                                        fontSize = 14.px
                                    }
                                }
                            }
                        }
                    }
                    right {
                        hbox {
                            paddingRight = 5.0
                            spacing = 10.0
                            button {
                                addClass(MediaPlayerStyles.mediaPlayerButton)
                                val icon = MaterialIconView(MaterialIcon.FEATURED_PLAY_LIST, "23px")
                                icon.fill = Color.LIGHTGRAY
                                icon.onHover {
                                    if (it) {
                                        icon.fill = Color.WHITE
                                    } else {
                                        if (isOverlayButtonChecked) {
                                            icon.fill = Color.LIGHTGRAY
                                        } else {
                                            icon.fill = Color.DIMGRAY
                                        }
                                    }
                                }
                                this.add(icon)
                                action {
                                    isOverlayButtonChecked = !isOverlayButtonChecked
                                }
                                tooltip("Show Chat Overlay")
                            }
                            button {
                                addClass(MediaPlayerStyles.chatSideButton)
                                val icon = MaterialIconView(MaterialIcon.CHAT_BUBBLE, "23px")
                                icon.fill = Color.LIGHTGRAY
                                icon.onHover {
                                    if (it) {
                                        icon.fill = Color.WHITE
                                    } else {
                                        if (isChatSideBarShown) {
                                            icon.fill = Color.LIGHTGRAY
                                        } else {
                                            icon.fill = Color.DIMGRAY
                                        }
                                    }
                                }
                                this.add(icon)
                                action {
                                    isChatSideBarShown = !isChatSideBarShown
                                    chatController.setChatShown(isChatSideBarShown)
                                }
                                tooltip("Show Chat Bar")
                            }
                            /**
                             * Fullscreen
                             */
                            button {
                                addClass(MediaPlayerStyles.fullScreenButton)
                                val icon = MaterialIconView(MaterialIcon.FULLSCREEN, "30px")
                                icon.fill = Color.LIGHTGRAY
                                icon.onHover {
                                    if (it) {
                                        icon.fill = Color.WHITE
                                    } else {
                                        icon.fill = Color.LIGHTGRAY
                                    }
                                }
                                this.add(icon)
                                action {
                                    currentStage?.isFullScreen = !currentStage?.isFullScreen!!
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    init {
        slider.setOnMouseReleased {
            onChange(slider.value)
        }
        volSlider.setOnMouseClicked {
            onVolumeChange(volSlider.value)
        }
        volSlider.setOnMouseDragReleased {
            onVolumeChange(volSlider.value)
        }
        volSlider.setOnMouseDragged {
            onVolumeChange(volSlider.value)
        }
    }

    fun setOnPlayCallback(cb: () -> Unit) {
        this.onPlay = cb
    }

    fun setOnPauseCallback(cb: () -> Unit) {
        this.onPause = cb
    }

    fun setOnChangeCallback(cb: (Double) -> Unit) {
        this.onChange = cb
    }

    fun setSliderPosition(value: Double) {
        if (duration != null) {
            val currentTimeDuration = value * (duration!!).toDouble()
            this.currentTime.value =
                getMillisecondsToHHMMSS(currentTimeDuration.toLong())
        }
        if (isSliderBeingDragged) {
            return
        }
        slider.value = value
    }

    fun setOnVolumeChange(cb: (value: Double) -> Unit) {
        this.onVolumeChange = cb
    }

    fun setVolume(volumeLevel: Double) {
        if (volumeLevel >= 0 || volumeLevel <= 100.0) {
            volSlider.value = volumeLevel
        }
    }

    fun isOverlayButtonChecked(): Boolean {
        return isOverlayButtonChecked
    }

    fun setTotalDuration(duration: Long) {
        this.duration = duration
        this.totalTime.value = getMillisecondsToHHMMSS(duration)
    }

    fun showPlayButtonForOnPause() {
        val icon = MaterialIconView(MaterialIcon.PLAY_ARROW, "30px")
        icon.fill = Color.LIGHTGRAY
        playPauseButton.getChildList()?.clear()
        playPauseButton.add(icon)
        playPauseButton.onHover {
            val content = playPauseButton.getChildList()?.first() as MaterialIconView?
            if (it) {
                content?.fill = Color.WHITE
            } else {
                content?.fill = Color.LIGHTGRAY
            }
        }
        isPaused = true
    }

    fun showPauseButtonForOnPlay() {
        val icon = MaterialIconView(MaterialIcon.PAUSE, "30px")
        icon.fill = Color.LIGHTGRAY
        playPauseButton.getChildList()?.clear()
        playPauseButton.add(icon)
        playPauseButton.onHover {
            val content = playPauseButton.getChildList()?.first() as MaterialIconView?
            if (it) {
                content?.fill = Color.WHITE
            } else {
                content?.fill = Color.LIGHTGRAY
            }
        }
        isPaused = false
    }
}

fun getMillisecondsToHHMMSS(duration: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    val mins = TimeUnit.MILLISECONDS.toMinutes(duration) - TimeUnit.HOURS.toMinutes(hours)
    val secs =
        TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.HOURS.toSeconds(hours) - TimeUnit.MINUTES.toSeconds(mins)
    return if (hours == 0L) {
        String.format("%d:%02d", mins, secs)
    } else {
        String.format("%d:%02d:%02d", hours, mins, secs)
    }
}
