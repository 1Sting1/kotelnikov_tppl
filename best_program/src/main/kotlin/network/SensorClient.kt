package network

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.yield
import model.Packet
import java.net.InetSocketAddress
import kotlin.coroutines.coroutineContext
import java.util.concurrent.CancellationException

class SensorClient(
    private val host: String,
    private val port: Int,
    private val packetSize: Int,
    private val parseLogic: (ByteArray) -> Packet,
    private val outputChannel: SendChannel<Packet>,
    private val socketFactory: (String, Int) -> SocketWrapper = RealSocketFactory()
) {
    private val authKey = "isu_pt".toByteArray(Charsets.US_ASCII)
    private val cmdGet = "get".toByteArray(Charsets.US_ASCII)

    suspend fun run() {
        try {
            while (coroutineContext.isActive) {
                try {
                    connectAndLoop()
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    println("[$port] Connection error: ${e.message}. Retrying")
                    delay(2000)
                }
            }
        } catch (e: CancellationException) {
        }
    }

    private suspend fun connectAndLoop() {
        socketFactory(host, port).use { socket ->
            socket.connect(InetSocketAddress(host, port), 10000)
            val out = socket.outputStream
            val inp = socket.inputStream

            out.write(authKey)
            out.write(cmdGet)
            out.flush()

            val junk = ByteArray(7)
            try {
                if (inp.readNBytes(junk, 0, 7) != 7) {
                    throw java.io.IOException("Auth response EOF")
                }
            } catch (e: Exception) {
                throw java.io.IOException("Auth response fail")
            }

            val buffer = ByteArray(packetSize)
            var read = inp.readNBytes(buffer, 0, packetSize)
            if (read != packetSize) throw java.io.EOFException("Incomplete first packet")

            try {
                outputChannel.send(parseLogic(buffer))
            } catch (e: IllegalArgumentException) {
                printErrorBytes("First packet checksum fail", buffer)
                throw java.io.IOException("Sync lost at start")
            }

            while (coroutineContext.isActive) {
                yield()
                out.write(cmdGet)
                out.flush()
                read = inp.readNBytes(buffer, 0, packetSize)
                if (read != packetSize) throw java.io.EOFException("Incomplete packet")

                try {
                    outputChannel.send(parseLogic(buffer))
                } catch (e: IllegalArgumentException) {
                    printErrorBytes("Checksum fail", buffer)
                    throw java.io.IOException("Data corruption")
                }
            }
        }
    }

    private fun printErrorBytes(msg: String, bytes: ByteArray) {
        val hex = bytes.joinToString(" ") { "%02X".format(it) }
        println("[$port] $msg. Data=[$hex]")
    }
}