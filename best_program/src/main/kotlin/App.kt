import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import model.Packet
import network.SensorClient
import network.SocketWrapper
import util.DataParser
import util.TimeUtil
import java.io.Writer

class App(
    private val host: String,
    private val writer: Writer,
    private val socketFactory: (String, Int) -> SocketWrapper,
    private val channel: Channel<Packet> = Channel(100)
) {
    suspend fun start(scope: CoroutineScope) {
        val parser = DataParser()

        scope.launch(Dispatchers.IO) { SensorClient(host, 5123, DataParser.WEATHER_SIZE, parser::parseWeather, channel, socketFactory).run() }
        scope.launch(Dispatchers.IO) { SensorClient(host, 5124, DataParser.VECTOR_SIZE, parser::parseVector, channel, socketFactory).run() }

        writer.use { out ->
            try {
                for (packet in channel) {
                    val line = "${TimeUtil.formatMicros(packet.timestamp)} -> ${packet.toLogString()}"
                    out.write(line)
                    out.write(System.lineSeparator())
                    out.flush()
                    println(line)
                }
            } catch (e: CancellationException) {
                println("\nApp stopped.")
            }
        }
    }
}