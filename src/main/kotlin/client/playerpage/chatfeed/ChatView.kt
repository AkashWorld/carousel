package client.playerpage.chatfeed

import client.Styles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ContentType
import client.models.Message
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.Observable
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.ListView
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.stage.StageStyle
import javafx.util.Duration
import org.slf4j.LoggerFactory
import tornadofx.*

class ChatView(
    private val chatController: ChatController,
    private val clientContextController: ClientContextController
) : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private var chatInput: SimpleStringProperty = SimpleStringProperty()
    private var listView: ListView<Message>? = null
    private val emojiLoader: EmojiLoader by inject()
    private val emojiPicker = find<EmojiPicker>("emojiCallback" to { alias: String ->
        emojiAliasCallback(alias)
    })

    override val root = borderpane {
        maxWidth = 370.0
        minHeight = Double.MAX_VALUE
        style {
            this.fontSize = Styles.chatFontSize
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
                button {
                    addClass(Styles.emojiButton)
                    val icon = MaterialIconView(MaterialIcon.CONTENT_COPY, "30px")
                    icon.fill = Styles.chatTextColor
                    icon.onHover {
                        if (it) {
                            icon.fill = Color.DARKGRAY
                        } else {
                            icon.fill = Styles.chatTextColor
                        }
                    }
                    action {
                        clientContextController.addressToClipboard()
                    }
                    this.add(icon)
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
                                        this.fill = Styles.chatTextColor
                                    }
                                }
                            }
                            it.getContent().split(" ").map {
                                val token = it
                                val image = emojiLoader.getEmojiFromAlias(it, 20.0)
                                if (image == null) {
                                    text(it) {
                                        style {
                                            this.fill = Styles.chatTextColor
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
                    left {
                        button {
                            addClass(Styles.emojiButton)
                            val icon = MaterialIconView(MaterialIcon.INSERT_EMOTICON, "30px")
                            icon.fill = Styles.chatTextColor
                            icon.onHover {
                                if (it) {
                                    icon.fill = Color.DARKGRAY
                                } else {
                                    icon.fill = Styles.chatTextColor
                                }
                            }
                            action {
                                val emojiStage = emojiPicker.openWindow(StageStyle.TRANSPARENT)
                                currentStage?.scene?.setOnMouseClicked {
                                    emojiStage?.close()
                                    currentStage?.scene?.onMouseClicked = null
                                }
                            }
                            this.add(icon)
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

    private fun emojiAliasCallback(alias: String) {
        val currentInput = chatInput.value ?: ""
        val alias = if (currentInput == "") alias else " $alias"
        chatInput.set(currentInput + alias)
    }

    private fun renderContent(content: String, contentType: ContentType, parent: Node) {
        if (contentType == ContentType.INFO) {
            text(content) {
                style {
                    fill = Styles.chatTextColor
                    fontStyle = FontPosture.ITALIC
                }
            }
        } else if (contentType == ContentType.MESSAGE) {
            content.split(" ").map {
                val token = it
                val image = emojiLoader.getEmojiFromAlias(it, 20.0)
                if (image == null) {
                    text(it) {
                        style {
                            this.fill = Styles.chatTextColor
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
                    parent.add(imageView)
                }
                text(" ")
            }
        }
    }
}

