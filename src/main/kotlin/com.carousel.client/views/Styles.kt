package com.carousel.client.views

import javafx.scene.paint.Color
import javafx.scene.paint.LinearGradient
import java.io.InputStream
import java.time.Instant
import kotlin.random.Random

class Styles {
    companion object {
        val defaultColor: Color = Color.valueOf("#db4057")
        val lightColor: Color = Color.valueOf("#ff4d67")
        val darkColor: Color = Color.valueOf("#a12d3e")
        val mainGradient: LinearGradient = LinearGradient.valueOf("from 0% 0% to 100% 100%, #7a2334, #3e091b")
        private val backgrounds =
            listOf("illuminated-street.jpg", "manhattenhenge.jpg", "night-time-city.jpg", "ny-cityscape.jpg")

        fun getRandomBackground(): InputStream? {
            val rand = Random(Instant.now().nano)
            val chosen = rand.nextInt(0, backgrounds.size)
            return this::class.java.classLoader.getResourceAsStream("backgrounds/${backgrounds[chosen]}")
        }

        fun getIconInputStream(): InputStream? {
            return this::class.java.classLoader.getResourceAsStream("icons/Carousel32.png")
        }
    }
}