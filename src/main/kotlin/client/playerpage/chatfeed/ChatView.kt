package client.playerpage.chatfeed

import client.Styles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.Message
import javafx.beans.Observable
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCombination
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.util.Duration
import org.slf4j.LoggerFactory
import tornadofx.*

private val CHAT_FONT_SIZE = 14.px
private val CHAT_TEXT_COLOR = Color.valueOf("#e3e3e3")

class ChatView(
    private val chatController: ChatController,
    private val clientContextController: ClientContextController
) : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var chatInput: SimpleStringProperty = SimpleStringProperty()
    private var listView: ListView<Message>? = null
    private var emojiLoader: Emoji = EmojiLoader()

    override val root = borderpane {
        maxWidth = 370.0
        minWidth = 370.0
        style {
            this.fontSize = CHAT_FONT_SIZE
            this.fontFamily = "Arial"
            this.backgroundColor = multi(Styles.chatBackgroundColor)
            this.borderColor = multi(
                box(Color(.27, .27, .27, 1.0))
            )
        }
        top {
            hbox {
                text("Server Address: ${clientContextController.getAddress().get()}") {
                    alignment = Pos.CENTER
                    style {
                        this.fontSize = 15.px
                        this.fill = Color(.878, .878, .878, 1.0)
                    }
                }
                style {
                    this.borderColor = multi(
                        box(
                            Color(.27, .27, .27, 1.0),
                            Color.TRANSPARENT,
                            Color(.27, .27, .27, 1.0),
                            Color.TRANSPARENT
                        )
                    )
                    this.prefHeight = 50.px
                }
            }
        }
        center {
            scrollpane(fitToWidth = true, fitToHeight = true) {
                style {
                    backgroundColor = multi(Color.TRANSPARENT)
                }
                listview(chatController.getMessages()) {
                    listView = this
                    this.scrollTo(chatController.getMessages().size)
                    this.items.addListener { _: Observable ->
                        listView!!.scrollTo(listView!!.items.size - 1)
                    }
                    this.heightProperty().addListener { _ -> chatController.padMessages(this.height) }
                    style {
                        this.focusColor = Color.TRANSPARENT
                        this.backgroundColor = multi(Styles.chatBackgroundColor)
                    }
                    cellFormat {
                        style {
                            this.focusColor = Color.TRANSPARENT
                            this.backgroundColor = multi(Styles.chatBackgroundColor)
                            this.padding = box(5.px)
                        }
                        graphic = textflow {
                            val textFlow = this
                            text(it.getUsername()) {
                                style {
                                    this.fill = chatController.getColor(it.getUsername())
                                    this.fontWeight = FontWeight.BOLD
                                }
                            }
                            if (it.getUsername() != "" && it.getContent() != "") {
                                text(": ") {
                                    style {
                                        this.fill = CHAT_TEXT_COLOR
                                    }
                                }
                            }
                            it.getContent().split(" ").map {
                                val token = it
                                val image = emojiLoader.getEmojiFromAlias(it)
                                if (image == null) {
                                    text(it) {
                                        style {
                                            this.fill = CHAT_TEXT_COLOR
                                        }
                                    }
                                } else {
                                    val imageView = object : ImageView(image) {
                                        override fun getBaselineOffset(): Double {
                                            return this.image.height * 0.75
                                        }
                                    }
                                    imageView.tooltip(token) {
                                        this.showDelay = Duration.ZERO
                                    }
                                    textFlow.add(imageView)
                                }
                                text(" ")
                            }
                            style {
                                this.maxWidth = 335.px
                            }
                        }
                    }
                }
            }
        }
        bottom {
            vbox {
                this.padding = Insets(10.0)
                this.spacing = 15.0
                textfield(chatInput) {
                    addClass(Styles.chatTextField)
                    this.promptText = "Send a message"
                    this.addEventFilter(KeyEvent.KEY_PRESSED) {
                        if (it.code == KeyCode.ENTER) {
                            sendChatMessage()
                        }
                    }
                }
                borderpane {
                    right {
                        button("Chat") {
                            addClass(Styles.chatButton)
                            action {
                                sendChatMessage()
                            }
                            shortcut("enter")
                        }
                    }
                }
            }
        }
    }

    init {
        chatController.subscribeToMessages()
    }

    private fun sendChatMessage() {
        if (chatInput.get() != null && chatInput.get() != "") {
            this.chatController.addMessage(chatInput.get())
            chatInput.set("")
        }
    }
}

