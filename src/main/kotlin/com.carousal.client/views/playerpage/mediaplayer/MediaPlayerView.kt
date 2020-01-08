package com.carousal.client.views.playerpage.mediaplayer

import com.carousal.client.controllers.ChatController
import com.carousal.client.controllers.FileLoaderController
import com.carousal.client.controllers.MediaController
import com.carousal.client.controllers.UsersController
import com.carousal.client.models.Action
import com.carousal.client.models.MediaAction
import com.carousal.client.models.MediaActionObservable
import com.carousal.client.views.ViewUtils
import com.carousal.client.views.playerpage.FileLoaderView
import com.carousal.client.views.playerpage.chatfeed.MessageFragment
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.value.ChangeListener
import javafx.concurrent.ScheduledService
import javafx.concurrent.Task
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.Cursor
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.layout.BorderPane
import javafx.scene.layout.Pane
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.scene.transform.Affine
import org.slf4j.LoggerFactory
import tornadofx.*
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.media.MediaRef
import uk.co.caprica.vlcj.media.TrackType
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventListener
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer
import java.time.Instant
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

class MediaPlayerView : View() {
    private val mediaController: MediaController by inject()
    private val chatController: ChatController by inject()
    private val fileLoaderController: FileLoaderController by inject()
    private val usersController: UsersController by inject()
    private var mediaPlayerFactory: MediaPlayerFactory? = null
    private var mediaPlayer: EmbeddedMediaPlayer? = null
    private lateinit var mediaCanvas: Canvas
    private lateinit var mediaPane: Pane
    private lateinit var controlPane: BorderPane
    private val controls = find<MediaPlayerControls>()
    private var mediaActionObservable: MediaActionObservable? = null
    private val mediaActionListener: ChangeListener<MediaAction?>
    private var canvasImageHeight = 0.0
    private var lastMouseMovedMilli = 0L
    private val hoverCheckerService: ScheduledService<Unit>
    private var isMouseInsideMediaPane: Boolean = false
    /**
     * This exists to stop the control's time slider from asking the media player to change position, and
     * then the media player from sending a callback to change the slider position
     */
    private var userRecentlyChangedPosition: Boolean = false

    override val root = stackpane {
        setMinSize(0.0, 0.0)
        alignment = Pos.CENTER
        mediaPane = this
        canvas {
            mediaCanvas = this
            this.widthProperty().bind(mediaPane.widthProperty())
            this.heightProperty().bind(mediaPane.heightProperty())
        }
        vbox {
            this.paddingLeft = 25.0
            this.paddingTop = 25.0
            chatController.getMessages().addListener { _: Observable ->
                if (!controls.isOverlayButtonChecked() || chatController.getMessages().isEmpty()) {
                    return@addListener
                }
                val message = chatController.getMessages().last()
                this.clear()
                this.add(find<MessageFragment>(params = mapOf("message" to message, "textSize" to 25.0)))
                runLater(5000.millis) {
                    if (chatController.getMessages().isEmpty() || message == chatController.getMessages().last()) {
                        this.clear()
                    }
                }
            }
        }
        controlPane = borderpane {
            bottom {
                this.add(controls)
                paddingBottom = 5.0
            }
        }
        /**
         * Controls should disappear if not hovering or mouse is still for too long
         */
        mediaPane.setOnMouseMoved {
            lastMouseMovedMilli = Instant.now().toEpochMilli()
            primaryStage.scene.cursor = Cursor.DEFAULT
            if (!mediaPane.children.contains(
                    controlPane
                )
            ) {
                mediaPane.add(controlPane)
            }
        }
        mediaPane.setOnMouseEntered { isMouseInsideMediaPane = true }
        mediaPane.setOnMouseExited { isMouseInsideMediaPane = false }
        mediaPane.onHover {
            if (!it) {
                runLater(2000.millis) {
                    if (!mediaPane.isHover) {
                        mediaPane.children.remove(controlPane)
                    }
                }
            }
        }
        mediaPane.setOnMouseClicked {
            if (it.clickCount == 2) {
                currentStage?.isFullScreen = !currentStage?.isFullScreen!!
            }
        }

        style {
            backgroundColor = multi(Color.BLACK)
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
                        if (Instant.now().toEpochMilli() - lastMouseMovedMilli > 2500 && isMouseInsideMediaPane
                            && mediaPane.children.contains(controlPane)
                        ) {
                            runLater {
                                primaryStage.scene.cursor = Cursor.NONE
                                mediaPane.children.remove(controlPane)
                            }
                        }
                    }
                }
            }
        }
        hoverCheckerService.period = 2500.millis
        mediaActionListener = ChangeListener { _, _, newValue ->
            newValue?.run { handleMediaAction(this) }
        }
    }

    override fun onDock() {
        super.onDock()
        setUpMediaPlayer()
        setUpControls()
        mediaController.subscribeToMediaActions {
            ViewUtils.showErrorDialog(
                "A connection error has occurred, could not sync video",
                primaryStage.scene.root as StackPane
            )
        }
        mediaActionObservable = mediaController.getMediaActionObservable()
        mediaActionObservable?.addListener(mediaActionListener)

        lastMouseMovedMilli = Instant.now().toEpochMilli()
        hoverCheckerService.start()
    }

    override fun onUndock() {
        super.onUndock()
        stopTimer()
        hoverCheckerService.cancel()
        hoverCheckerService.reset()
        mediaPlayerFactory?.release()
        mediaPlayer?.release()
        mediaPlayerFactory = null
        mediaPlayer = null
        mediaActionObservable?.removeListener(mediaActionListener)
        mediaActionObservable = null
        chatController.setChatShown(true)
        usersController.isReady.value = false
        clearCanvas(mediaCanvas)
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

            canvasImageHeight = scaledH

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

    private fun clearCanvas(canvas: Canvas) {
        val graphics: GraphicsContext = canvas.graphicsContext2D

        val width = canvas.width
        val height = canvas.height

        graphics.fill = Color.BLACK
        graphics.fillRect(0.0, 0.0, width, height)
    }

    private val nanoTimer: NanoTimer = object : NanoTimer(1000.0 / 60.0) {
        override fun onSucceeded() {
            renderFrame(mediaCanvas)
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

    private fun stopTimer() {
        Platform.runLater {
            if (nanoTimer.isRunning) {
                nanoTimer.cancel()
            }
        }
    }

    private fun handleMediaAction(action: MediaAction) {
        when (action.action) {
            Action.PAUSE -> {
                mediaPlayer?.controls()?.pause()
                controls.togglePause()
            }
            Action.PLAY -> {
                mediaPlayer?.controls()?.play()
                controls.togglePlay()
            }
            Action.SEEK -> {
                userRecentlyChangedPosition = false
                action.currentTime?.toLong()?.run { mediaPlayer?.controls()?.setTime(this) }
            }
        }
    }

    private fun setUpMediaPlayer() {
        mediaPlayerFactory = MediaPlayerFactory("--ffmpeg-hw")
        mediaPlayer = mediaPlayerFactory?.mediaPlayers()?.newEmbeddedMediaPlayer()
        mediaPlayer?.videoSurface()?.set(TornadoFXVideoSurface())
        mediaCanvas.widthProperty()
            ?.addListener { _: Observable? -> if (!mediaPlayer?.status()?.isPlaying!!) renderFrame(mediaCanvas) }
        mediaCanvas.heightProperty()
            ?.addListener { _: Observable? -> if (!mediaPlayer?.status()?.isPlaying!!) renderFrame(mediaCanvas) }
        mediaPlayer?.media()?.play(fileLoaderController.getCurrentSelectedFile()?.absolutePath)
        mediaPlayer?.events()?.addMediaPlayerEventListener(object : MediaPlayerEventListener {
            override fun positionChanged(mediaPlayer: MediaPlayer?, newPosition: Float) {
                runLater {
                    if (!userRecentlyChangedPosition) {
                        controls.setSliderPosition(newPosition.toDouble())
                    }
                }
            }

            override fun playing(mediaPlayer: MediaPlayer?) {}
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
            override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
                runLater {
                    if (mediaPlayer != null) {
                        mediaPlayer.controls().pause()
                        controls.togglePause()
                        mediaPlayer.audio().setVolume(100)
                        controls.setVolume(100.0)
                        controls.setTotalDuration(mediaPlayer.media().info().duration())
                    }
                }
            }

            override fun videoOutput(mediaPlayer: MediaPlayer?, newCount: Int) {}
            override fun error(mediaPlayer: MediaPlayer?) {
                runLater {
                    ViewUtils.showErrorDialog(
                        "An error occurred during video playback",
                        primaryStage.scene.root as StackPane
                    )
                    replaceWith<FileLoaderView>(ViewTransition.Fade(1000.millis))
                }
            }

            override fun mediaChanged(mediaPlayer: MediaPlayer?, media: MediaRef?) {}
            override fun finished(mediaPlayer: MediaPlayer?) {
                runLater {
                    replaceWith<FileLoaderView>(ViewTransition.Fade(1000.millis))
                }
            }

            override fun paused(mediaPlayer: MediaPlayer?) {}
            override fun timeChanged(mediaPlayer: MediaPlayer?, newTime: Long) {}
            override fun buffering(mediaPlayer: MediaPlayer?, newCache: Float) {}
            override fun lengthChanged(mediaPlayer: MediaPlayer?, newLength: Long) {}
        })
        startTimer()
    }

    private fun setUpControls() {
        controls.setOnPlayCallback {
            mediaController.playAction {
                ViewUtils.showErrorDialog(
                    "A connection error has occurred, could not sync video",
                    primaryStage.scene.root as StackPane
                )
            }
        }
        controls.setOnPauseCallback {
            mediaController.pauseAction {
                ViewUtils.showErrorDialog(
                    "A connection error has occurred, could not sync video",
                    primaryStage.scene.root as StackPane
                )
            }
        }
        controls.setOnChangeCallback {
            val newTime = mediaPlayer?.media()?.info()?.duration()?.times(it)
            newTime?.run {
                mediaController.seekAction(this.toFloat()) {
                    ViewUtils.showErrorDialog(
                        "A connection error has occurred, could not sync video",
                        primaryStage.scene.root as StackPane
                    )
                }
            }
            userRecentlyChangedPosition = true
        }
        controls.setOnVolumeChange {
            mediaPlayer?.audio()?.setVolume(it.toInt())
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
        pixelBuffer = PixelBuffer(
            bufferWidth,
            bufferHeight, buffers?.get(0),
            pixelFormat
        )
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
