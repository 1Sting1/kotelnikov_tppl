package model

sealed interface Packet {
    val timestamp: Long
    fun toLogString(): String
}

data class WeatherPacket(
    override val timestamp: Long,
    val temperature: Float,
    val pressure: Short
) : Packet {
    override fun toLogString(): String = "Temp=$temperature;Press=$pressure"
}

data class VectorPacket(
    override val timestamp: Long,
    val x: Int,
    val y: Int,
    val z: Int
) : Packet {
    override fun toLogString(): String = "X=$x;Y=$y;Z=$z"
}