package fx

import client.Styles
import client.playerpage.chatfeed.EmojiPicker
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.stage.StageStyle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*
import tornadofx.reloadStylesheetsOnFocus

class EmojiPickerFX {
    class TestApp : App(EmptyView::class, Styles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class EmptyView : View() {
        override val root = hbox {
            find<EmojiPicker>("emojiCallback" to { alias: String ->
                println("Emoji Alias Recieved -> ${alias}")
            }).openModal(StageStyle.TRANSPARENT)
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
        launch<TestApp>()
    }

    @Test
    fun emojiPickerShow() {

    }

}