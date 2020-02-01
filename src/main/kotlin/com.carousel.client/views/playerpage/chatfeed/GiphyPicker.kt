package com.carousel.client.views.playerpage.chatfeed

import com.carousel.client.controllers.ChatController
import com.carousel.client.controllers.GiphyController
import de.jensd.fx.glyphs.materialicons.MaterialIcon
import de.jensd.fx.glyphs.materialicons.MaterialIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.paint.Color
import tornadofx.*

class GiphyPicker : Fragment() {
    private val giphyController: GiphyController by inject()
    private val chatController: ChatController by inject()
    private val query = SimpleStringProperty("")

    override val root = borderpane {
        addClass(GiphyPickerStyles.giphyPickerContainer)
        top {
            hbox {
                textfield(query) {
                    addClass(ChatFeedStyles.emojiTextField)
                    promptText = "Powered by GIPHY"
                }
                button {
                    addClass(ChatFeedStyles.emojiButton)
                    val icon = MaterialIconView(MaterialIcon.SEARCH, "30px")
                    icon.fill = ChatFeedStyles.chatTextColor
                    icon.onHover {
                        if (it) {
                            icon.fill = Color.DARKGRAY
                        } else {
                            icon.fill = ChatFeedStyles.chatTextColor
                        }
                    }
                    action {
                        giphyController.retrieveSearchQueryGifs(query.value) {}
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
                        style {
                            this.focusColor = Color.TRANSPARENT
                            this.backgroundColor = multi(GiphyPickerStyles.gifBackgroundColor)
                            this.padding = box(5.px)
                            this.alignment = Pos.CENTER
                        }
                        val image = Image(this.item.url, 235.0, 235.0, true, true, true)
                        val iv = ImageView(image)
                        iv.addClass(GiphyPickerStyles.gifIVStyle)
                        graphic = iv
                        setOnMouseClicked {
                            chatController.addMessage(this.item.url)
                        }
                    }
                }
            }
        }
    }

    override fun onDock() {
        super.onDock()
        giphyController.retrieveTrendingGifs { }
    }
}

