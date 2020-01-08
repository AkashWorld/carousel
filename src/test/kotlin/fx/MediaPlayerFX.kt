package fx

import com.carousal.client.views.playerpage.FileLoaderStyles
import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousal.client.views.playerpage.FileLoaderView
import com.carousal.client.views.playerpage.mediaplayer.MediaPlayerStyles
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
