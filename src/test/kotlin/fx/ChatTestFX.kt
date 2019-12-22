package fx

import client.Styles
import client.controllers.ChatController
import client.models.ChatModel
import client.models.Message
import client.playerpage.chatfeed.ChatView
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*


class ChatTestFX {
    class TestApp: App(EmptyView::class, Styles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class EmptyView: View() {
        override val root = pane {
            val chatModel: ChatModel = ChatModel()
            chatModel.addMessage(Message("Lone Hunt", "Hey you, you are a good looking man"))
            chatModel.addMessage(Message("awildwildboar", "Thanks, this is cool"))
            chatModel.addMessage(Message("Lone Hunt", "Hey what do you think about this"))
            chatModel.addMessage(Message("Lone Hunt", "Its cool right?"))
            chatModel.addMessage(Message("dabessbeast", "yawn"))
            chatModel.addMessage(Message("wizardofozzie", "THIS IS THE GREATEST THING OF ALL TIME OMG"))
            chatModel.addMessage(Message("chauncey", "guys I need help starting up the copmuter"))
            chatModel.addMessage(Message("Anikzard", "stop being trash"))
            chatModel.addMessage(Message("Lone Hunt", "???????"))
            this.add(ChatView(ChatController(chatModel)))
        }
    }

    @BeforeEach
    fun start() {
        launch<TestApp>()
    }

    @Test
    fun chatTest() {

    }
}
