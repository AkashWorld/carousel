package fx

import com.carousel.client.views.playerpage.chatfeed.ChatFeedStyles
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerControls
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerStyles
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import javafx.scene.paint.Color
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import tornadofx.*

class MediaControlsFX {
    class TestApp :
        App(MediaControlsTestView::class, ChatFeedStyles::class, MediaPlayerStyles::class) {
        init {
            reloadStylesheetsOnFocus()
            reloadViewsOnFocus()
        }
    }

    class MediaControlsTestView : View() {
        override val root = borderpane {
            prefWidth = 800.0
            prefHeight = 400.0
            style {
                backgroundColor = multi(Color.BLACK)
            }
            top {
                rectangle(200, 200) {
                    fill = Color.RED
                }
            }
            center {
                val mediaControls = find<MediaPlayerControls>()
                this.add(mediaControls)
            }
        }
    }

    @BeforeEach
    fun initTornadoFX() {
        SvgImageLoaderFactory.install()
    }

    @Test
    fun mediaSliderTest() {
        launch<TestApp>()
    }

}