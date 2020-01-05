package client.controllers

import javafx.stage.FileChooser
import org.slf4j.LoggerFactory
import tornadofx.Controller
import tornadofx.chooseFile
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths

class FileLoaderController : Controller() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val videoFilter =
        FileChooser.ExtensionFilter("Video Extensions", "*.mkv", "*.mp4", "*.webm", "*.ogg", "*.flv", "*.wav", "*.avi")
    private var currentFile: File? = null
    private val mediaController: MediaController by inject()

    fun loadVideoFile(success: () -> Unit, error: (String?) -> Unit) {
        val videoFile = chooseFile("Choose video", arrayOf(videoFilter)) {
            val homeDir: String = System.getProperty("user.home")
            val videoFilePath = File("$homeDir/Videos")
            this.initialDirectory = videoFilePath
        }
        if (videoFile.size != 1) {
            logger.error("Could not load video")
            error("Please select a video to play")
            return
        }
        val filename = videoFile[0].name
        mediaController.loadMedia(filename, {
            this.currentFile = videoFile[0]
            success()
        }, { error("Could not sync file loading with the server") })
    }

    fun getCurrentSelectedFile(): File? {
        if (this.currentFile == null) {
            logger.error("There is no file selected, you must called FileChooser")
            return null
        }
        return this.currentFile
    }
}
