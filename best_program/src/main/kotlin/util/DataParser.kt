package util

import model.VectorPacket
import model.WeatherPacket
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DataParser {
    companion object {
        const val WEATHER_SIZE = 15
        const val VECTOR_SIZE = 21
        private val ORDER = ByteOrder.BIG_ENDIAN
    }

    fun parseWeather(bytes: ByteArray): WeatherPacket {
        validateChecksum(bytes)
        val buffer = ByteBuffer.wrap(bytes).order(ORDER)
        val time = buffer.long
        val temp = buffer.float
        val press = buffer.short
        return WeatherPacket(time, temp, press)
    }

    fun parseVector(bytes: ByteArray): VectorPacket {
        validateChecksum(bytes)
        val buffer = ByteBuffer.wrap(bytes).order(ORDER)
        val time = buffer.long
        val x = buffer.int
        val y = buffer.int
        val z = buffer.int
        return VectorPacket(time, x, y, z)
    }

    private fun validateChecksum(bytes: ByteArray) {
        var sum = 0
        for (i in 0 until bytes.size - 1) {
            sum += (bytes[i].toInt() and 0xFF)
        }

        val calculated = (sum % 256).toByte()
        val received = bytes.last()

        if (calculated != received) {
            throw IllegalArgumentException("Checksum mismatch")
        }
    }
}