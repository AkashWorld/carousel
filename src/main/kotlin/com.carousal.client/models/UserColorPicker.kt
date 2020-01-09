package com.carousal.client.models

import javafx.scene.paint.Color
import java.time.Instant
import kotlin.random.Random

class UserColorPicker {
    companion object {
        const val GOLDEN_RATIO_CONJUGATE = 0.618033988749895
    }
    private val userColorMap: MutableMap<String, Color> = mutableMapOf()
    private val rand = Random(Instant.now().nano)
    private var h: Double

    init {
        h = rand.nextDouble(0.0, 360.0)
    }

    fun getColorForUsername(username: String): Color {
        if(userColorMap.containsKey(username)) {
            return userColorMap[username] ?: error("")
        }
        val color = Color.hsb(h, .90, .99)
        h += GOLDEN_RATIO_CONJUGATE * 360
        h %= 360
        userColorMap[username] = color
        return color
    }
}