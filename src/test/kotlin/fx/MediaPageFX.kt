package fx

import client.views.playerpage.chatfeed.ChatFeedStyles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
import client.models.ClientContextImpl
import client.models.ContentType
import client.models.Message
import client.views.intropage.IntroPageStyles
import client.views.playerpage.FileLoaderStyles
import client.views.playerpage.PlayerPage
import client.views.playerpage.mediaplayer.MediaPlayerStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server
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
