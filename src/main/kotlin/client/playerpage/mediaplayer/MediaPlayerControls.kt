package client.playerpage.mediaplayer

import com.jfoenix.controls.JFXSlider
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
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
    private var duration: Long? = 0
    private var isPaused = false
    private var slider: JFXSlider? = null
    private var volSlider: JFXSlider? = null
    private var isSliderBeingDragged = false
    private var isOverlayButtonChecked = true
    override val root = borderpane {
        /**
         * Slider
         */
        center {
            vbox {
                slider = JFXSlider(0.0, 1.0, 0.0)
                slider?.addClass(MediaPlayerStyles.trackSlider)
                this.add(slider!!)
                borderpane {
                    paddingTop = 2.5
                    left {
                        hbox {
                            paddingLeft = 5.0
                            spacing = 5.0
                            /**
                             * Play Pause
                             */
                            button {
                                addClass(MediaPlayerStyles.mediaPlayerButton)
                                val initIcon = MaterialIconView(MaterialIcon.PAUSE, "30px")
                                initIcon.fill = Color.LIGHTGRAY
                                initIcon.onHover {
                                    if (it) {
                                        initIcon.fill = Color.WHITE
                                    } else {
                                        initIcon.fill = Color.LIGHTGRAY
                                    }
                                }
                                this.add(initIcon)
                                action {
                                    val icon: MaterialIconView = if (isPaused) {
                                        onPlay()
                                        MaterialIconView(MaterialIcon.PAUSE, "30px")
                                    } else {
                                        onPause()
                                        MaterialIconView(MaterialIcon.PLAY_ARROW, "30px")
                                    }
                                    icon.onHover {
                                        if (it) {
                                            icon.fill = Color.WHITE
                                        } else {
                                            icon.fill = Color.LIGHTGRAY
                                        }
                                    }
                                    icon.fill = Color.LIGHTGRAY
                                    isPaused = !isPaused
                                    this.getChildList()?.clear()
                                    this.add(icon)
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
                                    volSlider?.orientation = Orientation.HORIZONTAL
                                    volSlider?.addClass(MediaPlayerStyles.volumeSlider)
                                    volBox.onHover { isHover ->
                                        if (isHover) {
                                            icon.fill = Color.WHITE
                                            volBox.add(volSlider!!)
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
                            spacing = 5.0
                            button {
                                addClass(MediaPlayerStyles.mediaPlayerButton)
                                val icon = MaterialIconView(MaterialIcon.TEXTSMS, "23px")
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
                            }
                            /**
                             * Fullscreen
                             */
                            button {
                                addClass(MediaPlayerStyles.mediaPlayerButton)
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
        slider?.setOnMouseClicked {
            slider?.value?.let { it1 -> onChange(it1) }
        }
        slider?.setOnMouseDragEntered {
            isSliderBeingDragged = true
        }
        slider?.setOnMouseDragExited {
            isSliderBeingDragged = false
        }
        slider?.setOnMouseDragged {
            slider?.value?.let { it1 -> onChange(it1) }
        }
        volSlider?.setOnMouseClicked {
            volSlider?.value?.let { it1 -> onVolumeChange(it1) }
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
        slider?.value = value
    }

    fun setOnVolumeChange(cb: (value: Double) -> Unit) {
        this.onVolumeChange = cb
    }

    fun isOverlayButtonChecked(): Boolean {
        return isOverlayButtonChecked
    }

    fun setTotalDuration(duration: Long) {
        this.duration = duration
        this.totalTime.value = getMillisecondsToHHMMSS(duration)
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
