package com.carousel.client.views.playerpage.mediaplayer

import com.sun.jna.NativeLibrary
import org.slf4j.LoggerFactory
import uk.co.caprica.vlcj.binding.RuntimeUtil
import java.io.File

class VLCLibraryPathLoader {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val os = System.getProperty("os.name").toLowerCase()
    private val projectDir: String = System.getProperty("user.dir")

    init {
        if (isWindows()) {
            val libPath = "${projectDir}${File.separator}vlclibs${File.separator}win32"
            logger.info("VLC Library Path: ${libPath}")
            NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), libPath)
        } else if (isMac()) {
            //TODO Add apple support
        }
    }

    private fun isWindows(): Boolean {
        return os.contains("win")
    }

    private fun isMac(): Boolean {
        return os.contains("mac")
    }
}