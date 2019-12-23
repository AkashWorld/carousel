import client.Styles
import client.playerpage.PlayerPage
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import tornadofx.*

class Application: App(PlayerPage::class, Styles::class) {
    init {
        SvgImageLoaderFactory.install()
        reloadStylesheetsOnFocus()
    }
}

fun main(args: Array<String>) {
    //val server = Server()
    //server.initialize()
    launch<Application>()
}

