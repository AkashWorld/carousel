import client.playerpage.FileLoaderView
import tornadofx.*

class Application: App(FileLoaderView::class)

fun main(args: Array<String>) {
    launch<Application>()
}

