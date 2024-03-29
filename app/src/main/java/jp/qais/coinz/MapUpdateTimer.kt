package jp.qais.coinz

import timber.log.Timber
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule

/**
 * MapUpdateTimer will run a callback whenever a day progresses.
 *
 */
class MapUpdateTimer(private val callback: () -> Unit) {
    private var timer: TimerTask? = null
    private var target: Date? = null

    // Cancel old timer task if it exists
    fun stop() {
        timer?.cancel()
    }

    // Run our callback
    private fun run() {
        Timber.d("Executing callback")
        callback()
    }

    fun restart() {
        stop()

        // If we previous had a target
        target?.let {
            // And we have surpassed that target
            if (it.before(Date.from(Instant.now()))) {
                // Run the target
                run()
            }
        }

        // Start the runner again
        start()
    }

    private fun start() {
        // Set the target to be tomorrow
        target = Date.from(Utils.getTomorrow())?.also {
            // Schedule the callback runner
            timer = Timer().schedule(it) {
                this@MapUpdateTimer.run() // run the callback now
                this@MapUpdateTimer.start() // schedule the next one
            }
        }
    }
}