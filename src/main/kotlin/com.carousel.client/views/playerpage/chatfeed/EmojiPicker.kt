package com.carousel.client.views.playerpage.chatfeed

import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.paint.Color
import tornadofx.*

class EmojiPicker : Fragment() {
    val emojiCallback: (alias: String) -> Unit by param()
    private var search = SimpleStringProperty("")
    private val emojiLoader: EmojiLoader by inject()
    private val emojiSize = 24.0
    private val hoveredEmojiAlias = SimpleStringProperty("")
    private val emojiAliases = FXCollections.observableArrayList<String>(emojiLoader.defaultAliases())
    private val totalTiles = 30

    override val root = borderpane {
        addClass(ChatFeedStyles.emojiPickerContainer)
        style {
            backgroundColor = multi(ChatFeedStyles.chatBackgroundColor)
        }
        top {
            hbox {
                textfield(search) {
                    addClass(ChatFeedStyles.emojiTextField)
                    promptText = "Search Emoji"
                    search.onChange {
                        if (it != null) {
                            emojiLoader.setEmojiAliasesBySearch(it, emojiAliases)
                        }
                    }
                }
                button {
                    addClass(ChatFeedStyles.emojiButton)
                    val icon = MaterialIconView(MaterialIcon.CLOSE, "30px")
                    icon.fill = ChatFeedStyles.chatTextColor
                    icon.onHover {
                        if (it) {
                            icon.fill = Color.DARKGRAY
                        } else {
                            icon.fill = ChatFeedStyles.chatTextColor
                        }
                    }
                    action {
                        close()
                    }
                    this.add(icon)
                }
            }
        }
        center {
            scrollpane(true) {
                style {
                    focusColor = Color(.27, .27, .27, 1.0)
                    backgroundColor = multi(Color.TRANSPARENT)
                    padding = box(10.px)
                }
                tilepane {
                    val tilePane = this
                    style {
                        focusColor = Color.TRANSPARENT
                        backgroundColor = multi(ChatFeedStyles.chatBackgroundColor)
                        padding = box(10.px)
                    }
                    orientation = Orientation.HORIZONTAL
                    hgap = 25.0
                    vgap = 20.0
                    prefColumns = 5

                    /** Default */
                    emojiAliases.map { alias ->
                        val image = emojiLoader.getEmojiFromAlias(alias, emojiSize)
                        if (image != null) {
                            imageview(image) {
                                addClass(ChatFeedStyles.emojiPickerButton)
                                onHover { isHover ->
                                    if (isHover) {
                                        hoveredEmojiAlias.set(alias)
                                    } else {
                                        hoveredEmojiAlias.set("")
                                    }
                                }
                                setOnMouseClicked {
                                    emojiCallback(alias)
                                }
                            }
                        }
                    }

                    emojiAliases.onChange {
                        tilePane.clear()
                        it.list.map { alias ->
                            val image = emojiLoader.getEmojiFromAlias(alias, emojiSize)
                            if (image != null) {
                                imageview(image) {
                                    addClass(ChatFeedStyles.emojiPickerButton)
                                    onHover { isHover ->
                                        if (isHover) {
                                            hoveredEmojiAlias.set(alias)
                                        } else {
                                            hoveredEmojiAlias.set("")
                                        }
                                    }
                                    setOnMouseClicked {
                                        emojiCallback(alias)
                                    }
                                }
                            }
                        }
                        if (it.list.size < totalTiles) {
                            if (it.list.size == 0) {
                                for (i in 0..140) {
                                    rectangle {
                                        x = emojiSize
                                        y = emojiSize
                                    }
                                }
                            } else {
                                val neededTiles = totalTiles - it.list.size
                                for (i in 0..neededTiles) {
                                    rectangle {
                                        x = emojiSize
                                        y = emojiSize
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        bottom {
            hbox {
                spacing = 10.0
                prefHeight = 60.0
                val container = this
                label("Pick an emoji!") {
                    style {
                        paddingTop = 5
                        fontSize = 25.px
                        fill = ChatFeedStyles.chatTextColor
                    }
                }
                hoveredEmojiAlias.onChange {
                    container.clear()
                    val image = emojiLoader.getEmojiFromAlias(hoveredEmojiAlias.get(), 50.0)
                    if (image != null) {
                        imageview(image) {
                            paddingTop = 5
                            paddingHorizontal = 20
                        }
                        label(hoveredEmojiAlias.get()) {
                            style {
                                paddingTop = 5
                                fontSize = 25.px
                                fill = ChatFeedStyles.chatTextColor
                            }
                        }
                    } else {
                        label("Pick an emoji!") {
                            style {
                                paddingTop = 5
                                fontSize = 25.px
                                fill = ChatFeedStyles.chatTextColor
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        currentStage?.scene?.fill = Color.TRANSPARENT
    }
}