package fx

import com.carousal.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousal.client.views.playerpage.chatfeed.EmojiPicker
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*
import tornadofx.reloadStylesheetsOnFocus

class EmojiPickerFX {
    class TestApp : App(EmojiPickerTestView::class, ChatFeedStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class EmojiPickerTestView : View() {
        override val root = pane {
            val emojipicker = find<EmojiPicker>("emojiCallback" to { alias: String ->
                println("Emoji Alias Recieved -> ${alias}")
            })
            this.add(emojipicker)
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
    }

    @Test
    fun emojiPickerShow() {
        launch<TestApp>()
    }
}