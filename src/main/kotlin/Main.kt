import client.playerpage.FileLoaderStyles
import client.playerpage.mediaplayer.MediaPlayerStyles
import client.playerpage.chatfeed.ChatFeedStyles
import client.playerpage.PlayerPage
import de.codecentric.centerdevice.javafxsvg.SvgImageLoaderFactory
import server.Server
import tornadofx.*

class Application :
    App(
        PlayerPage::class,
        ChatFeedStyles::class,
        FileLoaderStyles::class,
        MediaPlayerStyles::class
    ) {
    init {
        SvgImageLoaderFactory.install()
        reloadStylesheetsOnFocus()
        reloadViewsOnFocus()
    }
}

fun main(args: Array<String>) {
    val server = Server()
    server.initialize()
    launch<Application>()
}

