package com.carousel.client.views.playerpage.chatfeed

import com.carousel.client.controllers.ChatController
import com.carousel.client.controllers.GiphyController
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import tornadofx.*

class GiphyPicker : Fragment() {
    private val giphyController: GiphyController by inject()
    private val chatController: ChatController by inject()
    private val query = SimpleStringProperty("")

    override val root = borderpane {
        addClass(GiphyPickerStyles.giphyPickerContainer)
        style {
            backgroundColor = multi(GiphyPickerStyles.gifBackgroundColor)
        }
        top {
            hbox {
                textfield(query) {
                    addClass(ChatFeedStyles.emojiTextField)
                    promptText = "Search GIPHY"
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
            scrollpane(fitToWidth = true, fitToHeight = true) {
                style {
                    backgroundColor = multi(Color.TRANSPARENT)
                }
                listview(giphyController.getActiveGifList()) {
                    addClass(GiphyPickerStyles.gifListView)
                    style {
                        this.focusColor = Color.TRANSPARENT
                        this.backgroundColor = multi(GiphyPickerStyles.gifBackgroundColor)
                    }
                    cellFormat {
                        addClass(GiphyPickerStyles.gifIVStyle)
                        val image = Image(this.item.url, 235.0, 235.0, true, true, true)
                        val iv = ImageView(image)
                        graphic = iv
                        setOnMouseClicked {
                            chatController.addImageUrl(this.item.url, {}, {})
                            close()
                        }
                    }
                }
            }
        }
        bottom {
            hbox {
                this.alignment = Pos.CENTER
                text("Powered by GIPHY") {
                    style {
                        fill = ChatFeedStyles.chatTextColor
                    }
                }
            }
        }
        this.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                giphyController.retrieveSearchQueryGifs(query.value) {}
            }
        }
    }

    override fun onDock() {
        super.onDock()
        giphyController.retrieveTrendingGifs { }
        currentStage?.scene?.fill = Color.TRANSPARENT
    }
}

