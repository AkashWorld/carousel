package fx

import tornadofx.*
import client.views.playerpage.chatfeed.ChatFeedStyles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
import client.models.ClientContextImpl
import client.models.ContentType
import client.models.Message
import client.views.playerpage.chatfeed.ChatFragment
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server

class ChatViewFX {
    private val server: Server = Server.getInstance()

    class Application : App(ChatViewTest::class, stylesheet = ChatFeedStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class ChatViewTest : View() {
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
            tornadofx.setInScope(chatController, testScope)
            tornadofx.setInScope(clientContextController, testScope)
            clientContext.requestSignInToken("test", "localhost", null, {}, {})
            Thread.sleep(1000)
        }

        override val root = hbox {
            val chatView = find<ChatFragment>(scope = testScope)
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
        Server.clear()
    }

    @Test
    fun chatViewTest() {
        launch<Application>()
    }
}