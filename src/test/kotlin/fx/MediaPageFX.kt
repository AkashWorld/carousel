package fx

import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousal.client.controllers.ChatController
import com.carousal.client.controllers.ClientContextController
import com.carousal.client.models.ClientContext
import com.carousal.client.models.ClientContextImpl
import com.carousal.client.models.ContentType
import com.carousal.client.models.Message
import com.carousal.client.views.intropage.IntroPageStyles
import com.carousal.client.views.playerpage.fileloader.FileLoaderStyles
import com.carousal.client.views.playerpage.PlayerPage
import com.carousal.client.views.playerpage.mediaplayer.MediaPlayerStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import com.carousal.server.Server
import tornadofx.*

class MediaPageFX {
    private val server: Server = Server.getInstance()

    class Application : App(
        MediaPageTest::class,
        ChatFeedStyles::class,
        FileLoaderStyles::class,
        MediaPlayerStyles::class,
        IntroPageStyles::class
    )

    class MediaPageTest : View() {
        private val testScope = Scope()
        private val clientContext: ClientContext = ClientContextImpl.getInstance()
        private val clientContextController = ClientContextController()
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
            tornadofx.setInScope(chatController, testScope)
            tornadofx.setInScope(clientContextController, testScope)
            clientContext.requestSignInToken("test", "localhost", null, {}, {})
            Thread.sleep(1000)
        }

        override val root = stackpane {
            this.add(find<PlayerPage>(scope = testScope))
        }
    }

    @BeforeEach
    private fun init() {
        server.initialize()
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
