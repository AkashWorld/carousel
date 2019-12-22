package client.playerpage

import client.controllers.FileLoaderController
import javafx.application.Platform
import javafx.beans.Observable
import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Pos
import javafx.geometry.Rectangle2D
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.image.PixelBuffer
import javafx.scene.image.PixelFormat
import javafx.scene.image.WritableImage
import javafx.scene.transform.Affine
import org.slf4j.LoggerFactory
import tornadofx.View
import tornadofx.canvas
import tornadofx.hbox
import tornadofx.vbox
import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer

private var start: Long = 0
private var totalFrameTime: Long = 0
private var maxFrameTime: Long = 0
private var bufferWidth: Int = 0
private var bufferHeight: Int = 0
private var frames: Int = 0
private var img: WritableImage? = null
private var updatedBuffer: Rectangle2D? = null
private var pixelBuffer: PixelBuffer<ByteBuffer?>? = null
private var pixelFormat = PixelFormat.getByteBgraPreInstance()
private var mediaCanvas: Canvas? = null
private val x: DoubleProperty = SimpleDoubleProperty()
private val y: DoubleProperty = SimpleDoubleProperty()
private val opacity: DoubleProperty = SimpleDoubleProperty()

class MediaPlayerView : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val fileLoaderController: FileLoaderController by inject()
    private val mediaPlayerFactory: MediaPlayerFactory = MediaPlayerFactory()
    private val mediaPlayer = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer()

    override val root = hbox {
        if(fileLoaderController.getCurrentSelectedFile() == null) {
            replaceWith<FileLoaderView>()
        }
        vbox {
            alignment = Pos.CENTER
            canvas {
                mediaCanvas = this
                this.width = 1000.0
                this.height = 800.0
                mediaPlayer.videoSurface().set(TornadoFXVideoSurface())
                mediaPlayer.media().play(fileLoaderController.getCurrentSelectedFile()?.absolutePath)
                this.widthProperty()
                    .addListener { _: Observable? -> if (!mediaPlayer.status().isPlaying) renderFrame(this) }
                this.heightProperty()
                    .addListener { _: Observable? -> if (!mediaPlayer.status().isPlaying) renderFrame(this) }
                startTimer()
            }
        }
    }

    private fun renderFrame(canvas: Canvas) {
        frames += 1
        val renderStart = System.currentTimeMillis()
        val graphics: GraphicsContext = canvas.graphicsContext2D

        val width = canvas.width
        val height = canvas.height

        graphics.fill = mainGradient
        graphics.fillRect(0.0, 0.0, width, height)

        if (img != null) {
            val imageWidth = img?.width
            val imageHeight = img?.height

            val sx = width / imageWidth!!
            val sy = height / imageHeight!!
            val sf = Math.min(sx, sy)

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
            val fps: Double = 1000.toDouble() * frames / (renderStart - start)
            val meanFrameTime: Double = totalFrameTime / frames.toDouble()
            logger.info("FPS: $fps, Mean Frame Time: $meanFrameTime")
            graphics.transform = ax
            if (renderStart - start > 1000) {
                val renderTime = System.currentTimeMillis() - renderStart
                maxFrameTime = Math.max(maxFrameTime, renderTime)
                totalFrameTime += renderTime
            }
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
            Platform.runLater {
                pixelBuffer?.updateBuffer { updatedBuffer }
            }
        }
    }
}

