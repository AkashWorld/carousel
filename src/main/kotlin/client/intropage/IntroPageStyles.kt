package client.intropage

import javafx.scene.layout.BackgroundPosition
import javafx.scene.layout.BackgroundRepeat
import javafx.scene.layout.BackgroundSize
import tornadofx.Stylesheet
import tornadofx.cssclass
import tornadofx.multi

class IntroPageStyles : Stylesheet() {
    companion object {
        val leftIntroPanel by cssclass()
    }

    init {
        val backgroundLocation = this::class.java.classLoader.getResource("red-cinema.jpg")?.toURI()
        leftIntroPanel {
            backgroundImage = multi(backgroundLocation!!)
            backgroundPosition = multi(BackgroundPosition.CENTER)
            backgroundRepeat = multi(Pair(BackgroundRepeat.SPACE, BackgroundRepeat.SPACE))
            backgroundSize = multi(
                BackgroundSize(
                    BackgroundSize.AUTO, BackgroundSize.AUTO,
                    true, true, false, true
                )
            )
        }
    }
}