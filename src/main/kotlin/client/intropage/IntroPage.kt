package client.intropage

import tornadofx.*
import tornadofx.View

class IntroPage : View() {
    override val root = vbox {
        addClass(IntroPageStyles.leftIntroPanel)
        text("Playtime") {
            style {
                fontSize = 150.px
            }
        }
        hbox {
            button("Host") {

            }
            button("Connect") {

            }
        }

    }
}

class HostForm : View() {
    override val root = borderpane {

    }
}

class SignInForm : View() {
    override val root = borderpane {

    }
}