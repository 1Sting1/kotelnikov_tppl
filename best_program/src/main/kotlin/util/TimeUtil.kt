package util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object TimeUtil {
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    fun formatMicros(micros: Long): String {
        val seconds = micros / 1_000_000
        val nanos = (micros % 1_000_000) * 1_000
        return formatter.format(Instant.ofEpochSecond(seconds, nanos))
    }
}