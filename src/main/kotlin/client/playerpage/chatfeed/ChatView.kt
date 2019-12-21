package client.playerpage.chatfeed

import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.paint.Color
import org.slf4j.LoggerFactory
import tornadofx.*

private val CHAT_FONT_SIZE = 15.px

class ChatView : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    private var chatInput: SimpleStringProperty = SimpleStringProperty()
    private val chatController: ChatController by inject()
    override val root = borderpane {
        maxWidth = 370.0
        minWidth = 370.0
        style {
            this.backgroundColor = multi(Color.valueOf("#262626"))
            this.borderColor = multi(
                box(Color(.27, .27, .27, 1.0))
            )
        }
        top {
            hbox {
                text("Server Address: 127.0.0.1") {
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
                    this.backgroundColor = multi(Color.TRANSPARENT)
                    this.focusColor = Color.TRANSPARENT
                }
                listview(chatController.getMessages()) {
                    this.maxWidth = 370.0
                    style {
                        this.focusColor = Color.TRANSPARENT
                        this.backgroundColor = multi(Color.valueOf("#262626"))
                    }
                    cellFormat {
                        style {
                            this.focusColor = Color.TRANSPARENT
                            this.backgroundColor = multi(Color.valueOf("#262626"))
                        }
                        graphic = textflow {
                            text(it.username) {
                                style {
                                    this.fill = Color.RED
                                    this.fontSize = CHAT_FONT_SIZE
                                }
                            }
                            text (": ") {
                                    style {
                                        this.fill = Color.WHITE
                                        this.fontSize = CHAT_FONT_SIZE
                                    }
                            }
                            text(it.message) {
                                style {
                                    this.fill = Color.WHITE
                                    this.fontSize = CHAT_FONT_SIZE
                                }
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
                    this.promptText = "Enter a message"
                    style {
                        this.backgroundColor = multi(Color.valueOf("#595959"))
                        this.textFill = Color.WHITE
                        this.minHeight = 45.px
                        this.prefWidth = 310.px
                        this.padding = box(10.px)
                    }
                }
                borderpane {
                    right {
                        button("Chat") {
                            style {
                                this.prefWidth = 50.px
                                this.prefHeight = 30.px
                                this.focusColor = Color.TRANSPARENT
                                this.backgroundColor = multi(Color.valueOf("#af3ddb"))
                                this.textFill = Color.WHITE
                            }
                        }
                    }
                }
            }
        }
    }
}