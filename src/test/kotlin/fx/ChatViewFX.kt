package fx

import client.playerpage.chatfeed.ChatFeedStyles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
import client.models.ClientContextImpl
import client.models.ContentType
import client.models.Message
import client.playerpage.FileLoaderView
import client.playerpage.chatfeed.ChatView
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server
import tornadofx.Scope
import tornadofx.*

class ChatViewFX {
    private val server: Server = Server()

    class Application : App(ChatViewTest::class, stylesheet = ChatFeedStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class ChatViewTest : View() {
        private val testScope = Scope()
        private val clientContext: ClientContext = ClientContextImpl()
        private val clientContextController = ClientContextController()
        private val chatController: ChatController by inject(params = mapOf("clientContext" to clientContext))

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
            val chatView = find<ChatView>(scope = testScope, params = mapOf("clientContext" to clientContext))
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