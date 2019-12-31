package fx

import client.playerpage.chatfeed.ChatFeedStyles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
import client.models.ContentType
import client.models.Message
import client.playerpage.FileLoaderView
import client.playerpage.MediaPlayerControlsStyles
import client.playerpage.chatfeed.ChatView
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server
import tornadofx.*

class MediaPageFX {
    private val server: Server = Server()

    class Application : App(MediaPageTest::class, ChatFeedStyles::class, MediaPlayerControlsStyles::class) {
    }

    class MediaPageTest : View() {
        private val testScope = Scope()
        private val chatController = ChatController()
        private val clientContextController = ClientContextController()
        private val clientContext: ClientContext = ClientContext("localhost:57423")

        init {
            val msglist = chatController.getMessages()
            msglist.add(Message("Dabessbeast", "This is wack, lets go to the real syncplay :angry:"))
            msglist.add(Message("Lone Hunt", ":pepe: Bruh I worked on this for 4 months :pepe:"))
            msglist.add(Message("awildwildboar", "paused at 3:54", ContentType.INFO))
            msglist.add(Message("awildwildboar", "paused at 0:00", ContentType.INFO))
            msglist.add(Message("awildwildboar", "loaded shady korean video", ContentType.INFO))
            msglist.add(Message("chauncey", "How do I even use this thing!!!!!"))
            msglist.add(Message("Wizardofozzie", "Spiderman is the greatest dont @ me :wink:"))
            tornadofx.setInScope(chatController, testScope)
            tornadofx.setInScope(clientContextController, testScope)
            clientContext.requestSignInToken("test", {}, {})
            Thread.sleep(1000)
        }

        override val root = hbox {
            prefWidth = 1500.0
            val chatView = find<ChatView>(scope = testScope, params = mapOf("clientContext" to clientContext))
            val fileLoaderView = find<FileLoaderView>()
            this.add(fileLoaderView)
            this.add(chatView)
        }
    }

    @BeforeEach
    private fun init() {
        server.initialize()
        SvgImageLoaderFactory.install()
    }

    @AfterEach
    private fun close() {
        server.close()
    }

    @Test
    fun chatViewTest() {
        launch<Application>()
    }
}
