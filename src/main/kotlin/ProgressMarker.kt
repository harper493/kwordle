/**
 * ProgressMarker class - output a string at a regular interval (expressed in seconds)
 * until close() is called. Intended to be used with the use() function.
 */

import java.io.Closeable
import kotlin.concurrent.*

class ProgressMarker(period: Long=3, char: String?=".") : Closeable {
    val timer = fixedRateTimer(period=period*1000, action={ char?.let{ print(char) }})

    override fun close() { timer.cancel() }
}