package fx

import client.Styles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ChatModel
import client.models.ClientContext
import client.models.Message
import client.playerpage.chatfeed.ChatView
import com.google.gson.Gson
import com.google.gson.JsonObject
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.beans.property.SimpleStringProperty
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import server.Server
import tornadofx.*


class ChatTestFX {
    class TestApp : App(EmptyView::class, Styles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class EmptyView : View() {
        override val root = pane {
            val chatModel = ChatModel()
            chatModel.addMessage(Message("Lone Hunt", "Hey :grinning:, you are a good looking man"))
            chatModel.addMessage(Message("awildwildboar", "Thanks, this is cool :sadpepe:"))
            chatModel.addMessage(Message("Lone Hunt", "Hey what do you think about this"))
            chatModel.addMessage(Message("Lone Hunt", "Its cool right?"))
            chatModel.addMessage(Message("dabessbeast", "yawn :wink:"))
            chatModel.addMessage(Message("wizardofozzie", "THIS IS THE GREATEST THING OF ALL TIME OMG"))
            chatModel.addMessage(
                Message(
                    "chauncey",
                    ":eggplant: :eggplant: :eggplant: :eggplant: :eggplant: :eggplant: :eggplant: "
                )
            )
            chatModel.addMessage(Message("Anikzard", "stop being trash :pepe:"))
            chatModel.addMessage(Message("Lone Hunt", "???????"))
            val server = Server()
            server.initialize()
            val gson = Gson()
            val signInMutation = """
            mutation SignInMutation(${"$"}username: String!) {
                signIn(username: ${"$"}username)
            }
        """.trimIndent()
            val variables = mapOf("username" to "Tester")
            val query = mapOf("query" to signInMutation, "variables" to variables)
            val body: RequestBody = gson.toJson(query).toRequestBody()
            val request = Request.Builder().post(body)
                .url("http://localhost:${server.port()}/graphql")
                .build()
            val client = OkHttpClient()
            client.newCall(request).execute().use { response ->
                val token = (gson.fromJson(response.body?.string(), JsonObject::class.java)
                    .get("data") as JsonObject).get("signIn").asString
                val clientContext = ClientContext(SimpleStringProperty("localhost:57423"), token)
                val clientContextController = ClientContextController(clientContext)
                this.add(ChatView(ChatController(chatModel, clientContext), clientContextController))
            }
        }
    }

    @BeforeEach
    fun start() {
        SvgImageLoaderFactory.install()
        launch<TestApp>()
    }

    @Test
    fun chatTest() {

    }
}
