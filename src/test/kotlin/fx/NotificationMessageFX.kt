package fx

import com.carousel.client.views.utilities.NotificationTabFragment
import com.carousel.client.views.utilities.UtilityStyles
import javafx.scene.paint.Color
import org.junit.jupiter.api.Test
import tornadofx.*

class NotificationMessageFX {
    class Application : App(Container::class, UtilityStyles::class) {
        init {
            reloadStylesheetsOnFocus()
        }
    }

    class Container : View() {
        override val root = borderpane {
            prefWidth = 500.0
            prefHeight = 500.0
            style {
                backgroundColor = multi(Color.BLACK)
            }
        }

        override fun onDock() {
            super.onDock()
            runLater(1000.millis) {
                root.center = find<NotificationTabFragment>(mapOf("message" to "Hey, this is a notification message; Hello, World! Hey this is kind of cool. What is this program!")).root
            }
        }
    }

    @Test
    fun showNotificationTest() {
        launch<Application>()
    }
}