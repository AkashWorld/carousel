package client.playerpage

import javafx.concurrent.ScheduledService
import javafx.concurrent.Task
import javafx.util.Duration
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory

abstract class NanoTimer(period: Double) : ScheduledService<Void>() {
    private val ONE_NANO: Long = 1000000000L
    private val ONE_NANO_INV: Float = 1f / 1000000000L
    private var startTime: Long = 0
    private var previousTime: Long = 0
    private var frameRate: Double = 0.0
    private var deltaTime: Double = 0.0

    init {
        this.setPeriod(Duration.millis(period))
        this.setExecutor(Executors.newCachedThreadPool(NanoThreadFactory()))
    }


    override fun createTask(): Task<Void?> {
        return object : Task<Void?>() {
            override fun call(): Void? {
                updateTimer()
                return null
            }
        }
    }

    fun getTime(): Long {
        return System.nanoTime() - startTime
    }

    fun getTimeAsSeconds(): Double {
        return (getTime() * ONE_NANO_INV).toDouble()
    }

    fun getDeltaTime(): Double {
        return deltaTime
    }

    public fun getFrameRate(): Double {
        return frameRate
    }

    override fun start() {
        super.start()
        if (startTime <= 0) {
            startTime = System.nanoTime()
        }
    }

    override fun reset() {
        super.reset()
        startTime = System.nanoTime()
        previousTime = getTime()
    }

    private fun updateTimer() {
        deltaTime = (getTime() - previousTime) * (1.0f / ONE_NANO).toDouble()
        frameRate = 1.0f / deltaTime
        previousTime = getTime()
    }

    override fun succeeded() {
        super.succeeded()
        onSucceeded()
    }

    override fun failed() {
        exception.printStackTrace(System.err)
        onFailed()
    }

    protected abstract fun onSucceeded()

    protected open fun onFailed() {}
}

private class NanoThreadFactory : ThreadFactory {
    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "NanoTimerThread")
        thread.priority = Thread.NORM_PRIORITY + 1
        thread.isDaemon = true
        return thread
    }
}

private class Time {
    fun formatTime(value: Long): String {
        var value = value
        value /= 1000
        val hours = value.toInt() / 3600
        var remainder = value.toInt() - hours * 3600
        val minutes = remainder / 60
        remainder -= minutes * 60
        val seconds = remainder
        return String.format("%d:%02d:%02d", hours, minutes, seconds)
    }
}
