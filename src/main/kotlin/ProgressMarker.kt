import java.io.Closeable
import kotlin.concurrent.*

class ProgressMarker(period: Long=3, char: String=".") : Closeable {
    val timer = fixedRateTimer(period=period*1000, action={ print(char) })

    override fun close() { timer.cancel() }
}