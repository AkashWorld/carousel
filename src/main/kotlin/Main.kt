import client.playerpage.PlayerPage
import server.Server
import tornadofx.*

class Application: App(PlayerPage::class)

fun main(args: Array<String>) {
    val server = Server()
    server.initialize()
    launch<Application>()
}

