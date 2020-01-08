package com.carousal.client.views.playerpage.chatfeed

import javafx.scene.image.Image
import org.imgscalr.Scalr
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.imageio.ImageIO

interface ImageLoader {
    fun getBase64EncodingOfImage(path: String, width: Int, height: Int): String?
    fun getImageFromBase64Encoding(encoding: String, width: Double, height: Double): Image?
}

class ImageLoaderImpl : ImageLoader {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

    override fun getBase64EncodingOfImage(path: String, width: Int, height: Int): String? {
        val extension = path.substringAfterLast(".")
        if (extension != "jpg" && extension != "jpeg" && extension != "png" && extension != "gif") {
            return null
        }
        return try {
            val imageData = Files.readAllBytes(Path.of(path))
            val encodedString = Base64.getEncoder().encodeToString(imageData)
            logger.info("Uncompressed ${encodedString.length * 2} bytes")
            val compressedString = tryCompressImage(path, 285, 285)
            compressedString?.run { logger.info("Compressed ${this.length * 2} bytes") }
            compressedString
        } catch (e: Exception) {
            logger.error(e.message, e.cause)
            null
        }
    }

    override fun getImageFromBase64Encoding(encoding: String, width: Double, height: Double): Image? {
        return try {
            val inputStream = Base64.getDecoder().decode(encoding).inputStream()
            Image(inputStream, width, height, true, false)
        } catch (e: Exception) {
            logger.error(e.message, e.cause)
            null
        }
    }

    private fun tryCompressImage(path: String, height: Int, width: Int): String? {
        val baos = ByteArrayOutputStream()
        val extension = path.substringAfterLast(".")
        val file = File(path)
        return if(extension != "gif") {
            val image = Scalr.resize(ImageIO.read(file), height, width)
            image.flush()
            ImageIO.write(image, extension, baos)
            Base64.getEncoder().encodeToString(baos.toByteArray())
        } else {
            val imageData = Files.readAllBytes(Path.of(path))
            Base64.getEncoder().encodeToString(imageData)
        }
    }
}