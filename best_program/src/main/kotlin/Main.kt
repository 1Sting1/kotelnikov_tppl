import network.RealSocketFactory
import java.io.File

fun main() {
    kotlinx.coroutines.runBlocking {
        val file = File("sensors_data.txt")
        val app = App(
            host = "95.163.237.76",
            writer = file.bufferedWriter(),
            socketFactory = RealSocketFactory()
        )
        app.start(this)
    }
}