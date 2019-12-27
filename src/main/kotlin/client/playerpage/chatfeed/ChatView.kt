package client.playerpage.chatfeed

import client.Styles
import client.controllers.ChatController
import client.controllers.ClientContextController
import client.models.ClientContext
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

class ChatView : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)
    private val clientContext: ClientContext by param()
    private val clientContextController: ClientContextController by inject(params = mapOf("clientContext" to clientContext))
    private val chatController: ChatController by inject(params = mapOf("clientContext" to clientContext))
    private val chatInput: SimpleStringProperty = SimpleStringProperty()
    private val serverAddress: SimpleStringProperty = SimpleStringProperty()
    private val emojiLoader: EmojiLoader by inject()
    private val emojiPicker = find<EmojiPicker>("emojiCallback" to { alias: String ->
        emojiAliasCallback(alias)
    })

    init {
        serverAddress.value = clientContextController.getAddress()
    }

    override val root = borderpane {
        maxWidth = 370.0
        minWidth = 370.0
        style {
            this.fontSize = Styles.chatFontSize
            this.fontFamily = "Arial"
            this.backgroundColor = multi(Styles.chatBackgroundColor)
            this.borderColor = multi(
                box(Color(.27, .27, .27, 1.0))
            )
        }
        top {
            borderpane {
                center {
                    text(serverAddress) {
                        style {
                            this.fontSize = 15.px
                            this.fill = Color(.878, .878, .878, 1.0)
                        }
                    }
                }
                right {
                    paddingTop = 10
                    paddingRight = 5
                    button {
                        addClass(Styles.emojiButton)
                        val icon = MaterialIconView(MaterialIcon.CONTENT_COPY, "25px")
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
                            serverAddress.set("Copied!")
                            runLater(Duration.millis(3000.0)) {
                                serverAddress.set(clientContextController.getAddress())
                            }
                        }
                        this.add(icon)
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
                    val listView = this
                    listView.scrollTo((chatController.getMessages().size) - 1)
                    items.addListener { _: Observable ->
                        listView.scrollTo((chatController.getMessages().size) - 1)
                    }
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
                            /**
                             * Username
                             */
                            if (it.contentType != ContentType.NONE) {
                                text(it.user) {
                                    style {
                                        this.fill = chatController.getColor(it.user)
                                        this.fontWeight = FontWeight.BOLD
                                    }
                                }
                                text(": ") {
                                    style {
                                        this.fill = Styles.chatTextColor
                                    }
                                }
                            }
                            /**
                             * Content
                             */
                            if (it.contentType == ContentType.INFO) {
                                text(it.content) {
                                    style {
                                        fill = Styles.chatTextColor
                                        fontStyle = FontPosture.ITALIC
                                    }
                                }
                            } else if (it.contentType == ContentType.MESSAGE) {
                                chatController.tokenizeMessage(it.content).map {
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
                                        this.add(imageView)
                                    }
                                }
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
                            setOnMouseClicked {
                                val emojiStage = emojiPicker.openWindow(StageStyle.TRANSPARENT)
                                emojiStage?.isAlwaysOnTop = true
                                emojiStage?.x = it.screenX
                                emojiStage?.y = it.screenY - 485.0
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

    private fun sendChatMessage() {
        if (chatInput.get() != null && chatInput.get() != "") {
            this.chatController.addMessage(chatInput.get())
            chatInput.set("")
        }
    }

    private fun emojiAliasCallback(alias: String) {
        val currentInput = chatInput.value ?: ""
        chatInput.set(currentInput + alias)
    }

    init {
        chatController.subscribeToMessages()
    }
}

