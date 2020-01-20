package fx

import com.carousel.client.controllers.ChatController
import com.carousel.client.controllers.ClientContextController
import com.carousel.client.controllers.UsersController
import com.carousel.client.models.ClientContextImpl
import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousel.client.views.playerpage.chatfeed.ChatFragment
import com.carousel.server.Server
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import tornadofx.*

class ReadyCheckFX {
    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            Server.getInstance().initialize(5000)
            Thread.sleep(1000)
            ClientContextImpl.getInstance().requestSignInToken("Test", "localhost:5000", null, {}, {})
            Thread.sleep(1000)
        }
    }

    class Application : App(ReadyCheckViewTest::class, stylesheet = ChatFeedStyles::class) {
        init {
            reloadStylesheetsOnFocus()
            reloadViewsOnFocus()
        }
    }

    class ReadyCheckViewTest : View() {
        private val testScope = Scope()
        private val clientContextController = ClientContextController()
        private val chatController: ChatController by inject()
        private val userController = UsersController()

        init {
            userController.subscribeToUsersAction { }
            tornadofx.setInScope(chatController, testScope)
            tornadofx.setInScope(userController, testScope)
            tornadofx.setInScope(clientContextController, testScope)
            Thread.sleep(1000)
        }

        override val root = stackpane {
            val chatView = find<ChatFragment>(scope = testScope)
            this.add(chatView)
        }
    }

    @Test
    fun runReadyCheckTest() {
        launch<Application>()
    }


}