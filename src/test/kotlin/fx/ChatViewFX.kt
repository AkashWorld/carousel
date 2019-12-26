package fx

import client.Styles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
import client.models.ContentType
import client.models.Message
import client.playerpage.chatfeed.ChatView
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server
import tornadofx.Scope
import tornadofx.*

class ChatViewFX {
    private val server: Server = Server()

    class Applicaton : App(ChatViewTest::class, stylesheet = Styles::class)

    class ChatViewTest : View() {
        private val testScope = Scope()
        val chatController = ChatController()
        val clientContextController = ClientContextController()
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

        override val root = stackpane {
            val chatView = find<ChatView>(scope = testScope, params = mapOf("clientContext" to clientContext))
            this.add(chatView)
        }
    }

    @BeforeEach
    private fun init() {
        server.initialize()
    }

    @AfterEach
    private fun close() {
        server.close()
    }

    @Test
    fun chatViewTest() {
        SvgImageLoaderFactory.install()
        launch<Applicaton>()
    }
}