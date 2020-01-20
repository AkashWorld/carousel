package fx

import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousel.client.controllers.ChatController
import com.carousel.client.models.ClientContextImpl
import com.carousel.client.models.ContentType
import com.carousel.client.models.Message
import com.carousel.client.views.intropage.IntroPageStyles
import com.carousel.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousel.client.views.playerpage.PlayerPage
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerStyles
import com.carousel.client.views.utilities.UtilityStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.carousel.server.Server
import tornadofx.*

class MediaPageFX {
    private val server: Server = Server.getInstance()

    class Application : App(
        MediaPageTest::class,
        ChatFeedStyles::class,
        FileLoaderStyles::class,
        MediaPlayerStyles::class,
        IntroPageStyles::class,
        UtilityStyles::class
    )

    class MediaPageTest : View() {
        private val chatController: ChatController by inject()

        init {
            val msglist = chatController.getMessages()
            msglist.add(Message("Dabessbeast", "This is wack, lets go to the real syncplay :angry:"))
            msglist.add(Message("Lone Hunt", ":pepe: Bruh I worked on this for 4 months :pepe:"))
            msglist.add(Message("awildwildboar", "paused at 3:54", ContentType.INFO))
            msglist.add(Message("awildwildboar", "paused at 0:00", ContentType.INFO))
            msglist.add(Message("awildwildboar", "loaded shady korean video", ContentType.INFO))
            msglist.add(Message("chauncey", "How do I even use this thing!!!!!"))
            msglist.add(Message("Wizardofozzie", "Spiderman is the greatest dont @ me :wink:"))
            msglist.add(Message("Brandino", "Sorry guys I gotta go watch anime in japan :pepe:"))
            msglist.add(Message("chauncey", "you aint real fam"))
            msglist.add(Message("awildwildboar", "paused at 3:54", ContentType.INFO))
            msglist.add(Message("awildwildboar", "paused at 0:00", ContentType.INFO))
            msglist.add(Message("awildwildboar", "loaded shady korean video", ContentType.INFO))
            msglist.add(Message("Wizardofozzie", "whats wrong with you"))
            Thread.sleep(1000)
        }

        override val root = stackpane {
            prefWidth = 1500.0
            prefHeight = 1000.0
            this.add(find<PlayerPage>())
        }
    }

    @BeforeEach
    private fun init() {
        server.initialize()
        Thread.sleep(100)
        ClientContextImpl.getInstance().requestSignInToken("TestUser", "localhost:57423", null, {}, {})
        Thread.sleep(100)
        SvgImageLoaderFactory.install()
    }

    @AfterEach
    private fun close() {
        ClientContextImpl.getInstance().clearContext()
        server.close()
    }

    @Test
    fun mediaPageViewTest() {
        launch<Application>()
    }
}
