package client.views.playerpage.chatfeed

import client.controllers.*
import client.models.ContentType
import client.models.Message
import client.views.ViewUtils
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.Observable
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.StageStyle
import javafx.util.Duration
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import tornadofx.*

class ChatFragment : Fragment() {
    private val clientContextController: ClientContextController by inject()
    private val chatController: ChatController by inject()
    private val usersController: UsersController by inject()
    private val chatInput: SimpleStringProperty = SimpleStringProperty()
    private val serverAddress: SimpleStringProperty = SimpleStringProperty("")
    private val emojiPicker = find<EmojiPicker>("emojiCallback" to { alias: String ->
        emojiAliasCallback(alias)
    })
    private lateinit var listView: ListView<Message>
    private val menuButton = find<DropDownMenuFragment>()

    override val root = borderpane {
        hgrow = Priority.NEVER
        maxWidth = 370.0
        minWidth = 370.0
        style {
            this.fontSize = ChatFeedStyles.chatFontSize
            this.fontFamily = "Arial"
            this.backgroundColor = multi(ChatFeedStyles.chatBackgroundColor)
            this.borderColor = multi(
                box(Color(.27, .27, .27, 1.0))
            )
        }
        top {
            borderpane {
                center {
                    paddingBottom = 10
                    text(serverAddress) {
                        style {
                            this.fontSize = 15.px
                            this.fill = Color(.878, .878, .878, 1.0)
                        }
                    }
                }
                left {
                    paddingTop = 10
                    paddingLeft = 5.0
                    button {
                        addClass(ChatFeedStyles.emojiButton)
                        val icon = MaterialIconView(MaterialIcon.CONTENT_COPY, "25px")
                        icon.fill = ChatFeedStyles.chatTextColor
                        icon.onHover {
                            if (it) {
                                icon.fill = Color.DARKGRAY
                            } else {
                                icon.fill = ChatFeedStyles.chatTextColor
                            }
                        }
                        action {
                            clientContextController.addressToClipboard()
                            serverAddress.set("Copied!")
                            runLater(Duration.millis(3000.0)) {
                                serverAddress.set("Server Address: ${clientContextController.getAddress()}")
                            }
                        }
                        this.add(icon)
                        tooltip("Only share this IP address with those you trust!") {
                            showDelay = Duration.ZERO
                        }
                    }
                }
                right {
                    this.add(menuButton)
                    paddingRight = 5.0
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
                    addClass(ChatFeedStyles.chatListView)
                    listView = this
                    listView.scrollTo((chatController.getMessages().size) - 1)
                    items.addListener { _: Observable ->
                        listView.scrollTo((chatController.getMessages().size) - 1)
                    }
                    style {
                        this.focusColor = Color.TRANSPARENT
                        this.backgroundColor = multi(ChatFeedStyles.chatBackgroundColor)
                    }
                    cellFormat {
                        style {
                            this.focusColor = Color.TRANSPARENT
                            this.backgroundColor = multi(ChatFeedStyles.chatBackgroundColor)
                            this.padding = box(5.px)
                        }
                        graphic = find<MessageFragment>(params = mapOf("message" to this.item, "textSize" to 15.0)).root
                    }
                }
            }
        }
        bottom {
            vbox {
                this.padding = Insets(10.0)
                this.spacing = 15.0
                textfield(chatInput) {
                    addClass(ChatFeedStyles.chatTextField)
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
                            addClass(ChatFeedStyles.chatButton)
                            action {
                                sendChatMessage()
                            }
                            shortcut("enter")
                        }
                    }
                    left {
                        hbox {
                            spacing = 10.0
                            button {
                                addClass(ChatFeedStyles.emojiButton)
                                val icon = MaterialIconView(MaterialIcon.CHECK_CIRCLE, "30px")
                                usersController.isReady.addListener { _, _, newValue ->
                                    if (newValue) {
                                        icon.fill = Color.GREEN
                                    } else {
                                        icon.fill = Color.RED
                                    }
                                }
                                icon.onHover {
                                    if (it) {
                                        if (usersController.isReady.value) {
                                            icon.fill = Color.DARKGREEN
                                        } else {
                                            icon.fill = Color.DARKRED
                                        }
                                    } else {
                                        if (usersController.isReady.value) {
                                            icon.fill = Color.GREEN
                                        } else {
                                            icon.fill = Color.RED
                                        }
                                    }
                                }
                                this.add(icon)
                                icon.fill = Color.RED
                                tooltip("Ready Check")
                                setOnMouseClicked {
                                    usersController.sendReadyCheck(
                                        {},
                                        {
                                            ViewUtils.showErrorDialog(
                                                "Could not send ready check",
                                                primaryStage.scene.root as StackPane
                                            )
                                        }
                                    )
                                }
                            }
                            button {
                                addClass(ChatFeedStyles.emojiButton)
                                val icon = MaterialIconView(MaterialIcon.INSERT_EMOTICON, "30px")
                                icon.fill = ChatFeedStyles.chatTextColor
                                icon.onHover {
                                    if (it) {
                                        icon.fill = Color.DARKGRAY
                                    } else {
                                        icon.fill = ChatFeedStyles.chatTextColor
                                    }
                                }
                                setOnMouseClicked {
                                    val emojiStage = emojiPicker.openWindow(StageStyle.TRANSPARENT)
                                    emojiStage?.isAlwaysOnTop = true
                                    emojiStage?.x = it.screenX
                                    emojiStage?.y = it.screenY - 485.0
                                    primaryStage.scene.setOnMouseClicked {
                                        emojiStage?.close()
                                    }
                                }
                                this.add(icon)
                            }
                            this.add(find<InsertImageButtonFragment>())
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

    override fun onDock() {
        super.onDock()
        serverAddress.value = "Server Address: ${clientContextController.getAddress()}"
    }

    override fun onUndock() {
        super.onUndock()
    }
}


