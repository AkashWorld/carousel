package fx

import com.carousal.client.models.ClientContextImpl
import com.carousal.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousal.client.views.playerpage.fileloader.FileLoaderView
import com.carousal.client.views.playerpage.mediaplayer.MediaPlayerStyles
import com.carousal.client.views.utilities.NotificationTabFragment
import com.carousal.client.views.utilities.UtilityStyles
import com.carousal.server.Server
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*

class MediaPlayerFX {
    class TestApp :
        App(TextView::class, UtilityStyles::class, FileLoaderStyles::class, ChatFeedStyles::class, MediaPlayerStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class TextView : View() {
        override val root = stackpane {
            this.add(find<FileLoaderView>())
            setOnMouseClicked {
                this.add(find<NotificationTabFragment>(mapOf("message" to "This is an example notifcation. Its what it would look like in the real world scenario. Please do something important.")))
            }
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
        Server.getInstance().initialize()
        ClientContextImpl.getInstance().requestSignInToken("", "localhost:57423", "", {}, {})
        Thread.sleep(100)
    }

    @Test
    fun mediaPlayerTest() {
        launch<TestApp>()
    }

}
