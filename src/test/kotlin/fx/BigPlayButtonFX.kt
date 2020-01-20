package fx

import com.carousel.client.views.playerpage.mediaplayer.BigPlayButtonFragment
import com.carousel.client.views.playerpage.mediaplayer.MediaPlayerStyles
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color
import org.junit.jupiter.api.Test
import tornadofx.*

class BigPlayButtonFX {

    class Application : App(RootPane::class, MediaPlayerStyles::class)

    class RootPane : View() {
        private val bigButton = find<BigPlayButtonFragment>()
        private var isPaused = false
        override val root = stackpane {
            prefWidth = 500.0
            prefHeight = 500.0
            background = Background(BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY, Insets.EMPTY))
            this.add(bigButton)
            setOnMouseClicked {
                isPaused = if (isPaused) {
                    bigButton.triggerPlay()
                    false
                } else {
                    bigButton.triggerPause()
                    true
                }
            }
        }
    }

    @Test
    fun mediaPageViewTest() {
        launch<Application>()
    }
}