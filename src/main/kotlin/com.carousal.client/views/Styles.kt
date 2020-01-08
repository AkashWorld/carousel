package client.views

import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import java.io.File
import java.net.URI
import java.time.Instant
import kotlin.random.Random

class Styles {
    companion object {
        val buttonColor: Color = Color.valueOf("#db4057")
        val lightButtonColor: Color = Color.valueOf("#ff4d67")
        val darkButtonColor: Color = Color.valueOf("#a12d3e")
        val mainGradient: LinearGradient = LinearGradient.valueOf("from 0% 0% to 100% 100%, #7a2334, #3e091b")

        fun getRandomBackground(): URI? {
            val backgroundLocationURI = this::class.java.classLoader.getResource("backgrounds")?.toURI() ?: return null
            val backgroundDirectory = File(backgroundLocationURI)
            val backgrounds = backgroundDirectory.listFiles()?.filter { it.isFile }?.toList() ?: return null
            val rand = Random(Instant.now().nano)
            val chosen = rand.nextInt(0, backgrounds.size)
            return backgrounds[chosen].toURI()
        }

        fun getIconPath(): String? {
            return this::class.java.classLoader.getResource("icons/CarousalIcon32.png")?.toString()
        }
    }
}