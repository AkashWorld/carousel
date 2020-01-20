package fx

import com.carousel.client.views.intropage.ConnectFormFragment
import com.carousel.client.views.intropage.HostFormFragment
import com.carousel.client.views.intropage.IntroPage
import com.carousel.client.views.intropage.IntroPageStyles
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import com.carousel.server.Server
import tornadofx.*
import tornadofx.App
import tornadofx.launch

class IntroViewFX {
    class TestApp : App(TestView::class, IntroPageStyles::class) {
        init {
            reloadViewsOnFocus()
            reloadStylesheetsOnFocus()
        }
    }

    class TestView : View() {
        override val root = stackpane {
            prefWidth = 1000.0
            prefHeight = 800.0
            this.add(find<IntroPage>())
        }
    }

    @Test
    fun introViewTest() {
        launch<TestApp>()
    }

    class ConnectApp : App(TestView2::class, IntroPageStyles::class) {
        init {
            reloadViewsOnFocus()
            reloadStylesheetsOnFocus()
        }
    }

    class TestView2 : View() {
        override val root = stackpane {
            this.add(find<ConnectFormFragment>())
        }
    }

    @Test
    fun connectFragmentTest() {
        val server = Server.getInstance()
        server.initialize()
        launch<ConnectApp>()
    }

    class HostApp : App(TestView3::class, IntroPageStyles::class) {
        init {
            reloadViewsOnFocus()
            reloadStylesheetsOnFocus()
        }
    }

    class TestView3 : View() {
        override val root = stackpane {
            this.add(find<HostFormFragment>())
        }
    }

    @Test
    fun hostFragmentTest() {
        launch<HostApp>()
    }

    @AfterEach
    fun cleanServer() {
        Server.clear()
    }
}