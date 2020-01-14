package fx

import com.carousal.client.views.utilities.NotificationTabFragment
import com.carousal.client.views.utilities.UtilityStyles
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
        override val root = stackpane {
            prefWidth = 500.0
            prefHeight = 500.0
            style {
                backgroundColor = multi(Color.BLACK)
            }
        }

        override fun onDock() {
            super.onDock()
            runLater(1000.millis) {
                root.add(find<NotificationTabFragment>(mapOf("message" to "Hey, this is a notification message; Hello, World! Hey this is kind of cool. What is this program!")))
            }
        }
    }

    @Test
    fun showNotificationTest() {
        launch<Application>()
    }
}