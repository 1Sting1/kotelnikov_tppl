package network

import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

interface SocketWrapper : AutoCloseable {
    val inputStream: InputStream
    val outputStream: OutputStream
    fun connect(endpoint: InetSocketAddress, timeout: Int) }

class RealSocketFactory : (String, Int) -> SocketWrapper {
    override fun invoke(host: String, port: Int): SocketWrapper {
        return object : SocketWrapper {
            private val socket = Socket()
            override val inputStream: InputStream get() = socket.getInputStream()
            override val outputStream: OutputStream get() = socket.getOutputStream()

            override fun connect(endpoint: InetSocketAddress, timeout: Int) {
                socket.connect(endpoint, timeout)
                socket.soTimeout = 5000
            }
            override fun close() = socket.close()
        }
    }
}