package fx

import client.intropage.ConnectFormFragment
import client.intropage.IntroPage
import client.intropage.IntroPageStyles
import org.junit.jupiter.api.Test
import server.Server
import tornadofx.*
import tornadofx.App
import tornadofx.launch

class IntroViewFX {
    class TestApp : App(TestView::class, IntroPageStyles::class)

    class TestView : View() {
        override val root = stackpane{
            prefWidth = 1000.0
            prefHeight = 800.0
            this.add(find<IntroPage>())
        }
    }

    @Test
    fun introViewTest() {
        launch<TestApp>()
    }

    class ConnectApp : App(TestView2::class, IntroPageStyles::class)

    class TestView2 : View() {
        override val root = stackpane{
            this.add(find<ConnectFormFragment>())
        }
    }

    @Test
    fun connectFragmentTest() {
        val server = Server()
        server.initialize()
        launch<ConnectApp>()
    }
}