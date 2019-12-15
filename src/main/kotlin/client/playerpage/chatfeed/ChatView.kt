package client.playerpage.chatfeed

import org.slf4j.LoggerFactory
import tornadofx.*

class ChatView : View() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName);
    override val root = vbox {
        title = "chatview"
        maxWidth = 370.0
        minWidth = 370.0
        prefHeight = 1200.0
        text("Hello") {
            style {
                this.fontSize = 200.px
            }
        }
    }
}