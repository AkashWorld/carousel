package fx

import client.intropage.IntroPage
import client.intropage.IntroPageStyles
import org.junit.jupiter.api.Test
import tornadofx.*
import tornadofx.App
import tornadofx.launch

class IntroViewFX {
    class TestApp : App(TestView::class, IntroPageStyles::class)

    class TestView : View() {
        override val root = pane{
            prefWidth = 1000.0
            prefHeight = 800.0
            this.add(find<IntroPage>())
        }
    }

    @Test
    fun introViewTest() {
        launch<TestApp>()
    }
}