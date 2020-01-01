package fx

import client.playerpage.FileLoaderStyles
import client.playerpage.chatfeed.ChatFeedStyles
import client.playerpage.FileLoaderView
import client.playerpage.mediaplayer.MediaPlayerStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*

class MediaPlayerFX {
    class TestApp :
        App(FileLoaderView::class, FileLoaderStyles::class, ChatFeedStyles::class, MediaPlayerStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
    }

    @Test
    fun mediaPlayerTest() {
        launch<TestApp>()
    }

}
