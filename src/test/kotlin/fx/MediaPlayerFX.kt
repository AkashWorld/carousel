package fx

import com.carousel.client.models.ClientContextImpl
import com.carousel.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousel.client.views.playerpage.fileloader.FileLoaderView
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerStyles
import com.carousel.client.views.utilities.UtilityStyles
import com.carousel.server.Server
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*

class MediaPlayerFX {
    class TestApp :
        App(TestView::class, UtilityStyles::class, FileLoaderStyles::class, ChatFeedStyles::class, MediaPlayerStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class TestView : View() {
        override val root = stackpane {
            prefWidth = 1000.0
            prefHeight = 1000.0
            this.add(find<FileLoaderView>())
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
        Server.getInstance().initialize()
        Thread.sleep(100)
        ClientContextImpl.getInstance().requestSignInToken("TestUser", "localhost:57423", "", {}, {})
        Thread.sleep(100)
    }

    @Test
    fun mediaPlayerTest() {
        launch<TestApp>()
    }
}
