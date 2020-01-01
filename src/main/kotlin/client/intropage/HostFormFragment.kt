package client.intropage

import org.slf4j.LoggerFactory
import tornadofx.Fragment
import tornadofx.addClass
import tornadofx.borderpane

class HostFormFragment : Fragment() {
    private val logger = LoggerFactory.getLogger(this::class.qualifiedName)

    override val root = borderpane {
        addClass(IntroPageStyles.rightFormPanel)
    }
}