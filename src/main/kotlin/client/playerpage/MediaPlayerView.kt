package client.playerpage

import client.controllers.FileLoaderController
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.concurrent.ScheduledService
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.transform.Affine
import org.slf4j.LoggerFactory
import tornadofx.*
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.*
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener
import uk.co.caprica.vlcj.player.base.State
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule
import kotlin.concurrent.timerTask
import kotlin.math.min

/**
 * VLC MediaPlayer related data, needs to be global due to being shared between interfaces/views
 */
private var bufferWidth: Int = 0
private var bufferHeight: Int = 0
private var frames: Int = 0
private var img: WritableImage? = null
private var updatedBuffer: Rectangle2D? = null
private var pixelBuffer: PixelBuffer<ByteBuffer?>? = null
private var pixelFormat = PixelFormat.getByteBgraPreInstance()
private var mediaCanvas: Canvas? = null

class MediaPlayerView : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val fileLoaderController: FileLoaderController by inject()
    private val mediaPlayerFactory: MediaPlayerFactory = MediaPlayerFactory("--ffmpeg-hw")
    private val mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer()
    private lateinit var mediaPane: StackPane
    private lateinit var controlPane: BorderPane
    private var hoverCheckerService: ScheduledService<Unit>
    private val controls = find<MediaPlayerControls>()
    private var lastMouseMovedMilli = 0L
    /**
     * This exists to stop the control's time slider from asking the media player to change position, and
     * then the media player from sending a callback to change the slider position
     */
    private var userRecentlyChangedPosition: Boolean = false

    override val root = stackpane {
        mediaPane = this
        alignment = Pos.CENTER
        canvas {
            mediaCanvas = this
            this.widthProperty().bind(mediaPane.widthProperty())
            this.heightProperty().bind(mediaPane.heightProperty())
            mediaPlayer.videoSurface().set(TornadoFXVideoSurface())
            this.widthProperty()
                .addListener { _: Observable? -> if (!mediaPlayer.status().isPlaying) renderFrame(this) }
            this.heightProperty()
                .addListener { _: Observable? -> if (!mediaPlayer.status().isPlaying) renderFrame(this) }
        }
        controlPane = borderpane {
            bottom {
                this.add(controls)
            }
        }
        /**
         * Controls should disappear if not hovering or mouse is still for too long
         */
        mediaPane.onHover {
            if (it && Instant.now().toEpochMilli() - lastMouseMovedMilli < 5000 && !mediaPane.children.contains(
                    controlPane
                )
            ) {
                logger.info("OnHover - ${Instant.now().toEpochMilli()}")
                mediaPane.add(controlPane)
            } else {
                runLater(2000.millis) {
                    if (!mediaPane.isHover) {
                        mediaPane.children.remove(controlPane)
                    }
                }
            }
        }
        setOnMouseMoved {
            lastMouseMovedMilli = Instant.now().toEpochMilli()
        }
    }

    init {
        /**
         * Scheduled service that checks if mouse is staying still
         */
        hoverCheckerService = object : ScheduledService<Unit>() {
            override fun createTask(): Task<Unit> {
                return object : Task<Unit>() {
                    override fun call() {
                        if (Instant.now().toEpochMilli() - lastMouseMovedMilli > 5000 && mediaPane.children.contains(
                                controlPane
                            )
                        ) {
                            mediaPane.children.remove(controlPane)
                        }
                    }
                }
            }
        }
        hoverCheckerService.period = 5000.millis
        controls.setOnPlayCallback {
            mediaPlayer.controls().play()
        }
        controls.setOnPauseCallback {
            mediaPlayer.controls().pause()
        }
        controls.setOnChangeCallback {
            mediaPlayer.controls().setPosition(it.toFloat())
            userRecentlyChangedPosition = true
        }
        controls.setOnVolumeChange {
            mediaPlayer.audio().setVolume(it.toInt())
        }
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventListener {
            override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                runLater {
                    if (!userRecentlyChangedPosition) {
                        controls.setSliderPosition(newPosition.toDouble())
                    }
                    userRecentlyChangedPosition = false
                }
            }

            override fun playing(mediaPlayer: MediaPlayer?) {
                runLater {
                    if (mediaPlayer != null) {
                        controls.setTotalDuration(mediaPlayer.media().info().duration())
                        mediaPlayer.audio().setVolume(100)
                    }
                }
            }

            override fun audioDeviceChanged(mediaPlayer: MediaPlayer?, audioDevice: String?) {}
            override fun volumeChanged(mediaPlayer: MediaPlayer?, volume: Float) {}
            override fun scrambledChanged(mediaPlayer: MediaPlayer?, newScrambled: Int) {}
            override fun elementaryStreamSelected(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {}
            override fun seekableChanged(mediaPlayer: MediaPlayer?, newSeekable: Int) {}
            override fun stopped(mediaPlayer: MediaPlayer?) {}
            override fun snapshotTaken(mediaPlayer: MediaPlayer?, filename: String?) {}
            override fun muted(mediaPlayer: MediaPlayer?, muted: Boolean) {}
            override fun forward(mediaPlayer: MediaPlayer?) {}
            override fun pausableChanged(mediaPlayer: MediaPlayer?, newPausable: Int) {}
            override fun titleChanged(mediaPlayer: MediaPlayer?, newTitle: Int) {}
            override fun corked(mediaPlayer: MediaPlayer?, corked: Boolean) {}
            override fun chapterChanged(mediaPlayer: MediaPlayer?, newChapter: Int) {}
            override fun elementaryStreamDeleted(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {}
            override fun opening(mediaPlayer: MediaPlayer?) {}
            override fun backward(mediaPlayer: MediaPlayer?) {}
            override fun elementaryStreamAdded(mediaPlayer: MediaPlayer?, type: TrackType?, id: Int) {}
            override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {}
            override fun videoOutput(mediaPlayer: MediaPlayer?, newCount: Int) {}
            override fun error(mediaPlayer: MediaPlayer?) {}
            override fun mediaChanged(mediaPlayer: MediaPlayer?, media: MediaRef?) {}
            override fun finished(mediaPlayer: MediaPlayer?) {
                runLater {
                    stopTimer()
                    replaceWith<FileLoaderView>(ViewTransition.Fade(1000.millis))
                }
            }

            override fun paused(mediaPlayer: MediaPlayer?) {}
            override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {}
            override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {}
            override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {}
        })
    }

    override fun onDock() {
        super.onDock()
        mediaPlayer.media().play(fileLoaderController.getCurrentSelectedFile()?.absolutePath)
        startTimer()
        hoverCheckerService.start()
    }

    override fun onUndock() {
        super.onUndock()
        stopTimer()
        hoverCheckerService.cancel()
        hoverCheckerService.reset()
    }

    private fun renderFrame(canvas: Canvas) {
        frames += 1
        val graphics: GraphicsContext = canvas.graphicsContext2D

        val width = canvas.width
        val height = canvas.height

        graphics.fill = Color.BLACK
        graphics.fillRect(0.0, 0.0, width, height)

        if (img != null) {
            val imageWidth = img?.width
            val imageHeight = img?.height

            val sx = width / imageWidth!!
            val sy = height / imageHeight!!
            val sf = min(sx, sy)

            val scaledW = imageWidth * sf
            val scaledH = imageHeight * sf

            val ax: Affine = graphics.transform

            graphics.translate(
                (width - scaledW) / 2,
                (height - scaledH) / 2
            )

            if (sf != 1.0) {
                graphics.scale(sf, sf)
            }
            graphics.drawImage(img, 0.0, 0.0)
            graphics.transform = ax
        }
    }

    private val nanoTimer: NanoTimer = object : NanoTimer(1000.0 / 60.0) {
        override fun onSucceeded() {
            mediaCanvas?.let { renderFrame(it) }
        }
    }

    private fun startTimer() {
        Platform.runLater {
            if (!nanoTimer.isRunning) {
                nanoTimer.reset()
                nanoTimer.start()
            }
        }
    }

    private fun pauseTimer() {
        Platform.runLater {
            if (nanoTimer.isRunning) {
                nanoTimer.cancel()
            }
        }
    }

    private fun stopTimer() {
        Platform.runLater {
            if (nanoTimer.isRunning) {
                nanoTimer.cancel()
            }
        }
    }

    private class TornadoFXVideoSurface internal constructor() : CallbackVideoSurface(
        TornadoFXBufferFormatCallback(),
        TornadoFXRenderCallback(),
        true,
        VideoSurfaceAdapters.getVideoSurfaceAdapter()
    )

    class TornadoFXBufferFormatCallback : BufferFormatCallback {
        override fun allocatedBuffers(buffers: Array<out ByteBuffer>?) {
            pixelBuffer = PixelBuffer(bufferWidth, bufferHeight, buffers?.get(0), pixelFormat)
            img = WritableImage(pixelBuffer)
            updatedBuffer = Rectangle2D(0.0, 0.0, bufferWidth.toDouble(), bufferHeight.toDouble())
        }

        override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
            bufferWidth = sourceWidth
            bufferHeight = sourceHeight
            return RV32BufferFormat(sourceWidth, sourceHeight)
        }
    }

    class TornadoFXRenderCallback : RenderCallback {
        override fun display(
            mediaPlayer: MediaPlayer?,
            nativeBuffers: Array<out ByteBuffer>?,
            bufferFormat: BufferFormat?
        ) {
            runLater {
                pixelBuffer?.updateBuffer { updatedBuffer }
            }
        }
    }
}

