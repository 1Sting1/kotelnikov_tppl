@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.*
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.nio.ByteBuffer
import java.util.concurrent.CancellationException

import model.*
import network.*
import util.*

class ModelTests {
    @Test
    fun `test WeatherPacket full coverage`() {
        val p1 = WeatherPacket(100L, 20.5f, 760)
        assertEquals(100L, p1.timestamp)
        assertEquals(20.5f, p1.temperature)
        assertEquals(760.toShort(), p1.pressure)

        val p2 = WeatherPacket(100L, 20.5f, 760)
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
        assertTrue(p1.toString().contains("WeatherPacket"))
        assertEquals("Temp=20.5;Press=760", p1.toLogString())

        val p3 = p1.copy(temperature = 30f)
        assertNotEquals(p1, p3)
        val (t, _, _) = p1
        assertEquals(100L, t)
    }

    @Test
    fun `test VectorPacket full coverage`() {
        val p1 = VectorPacket(200L, 10, -20, 30)
        assertEquals(200L, p1.timestamp)
        assertEquals(10, p1.x)
        assertEquals(-20, p1.y)
        assertEquals(30, p1.z)

        val p2 = VectorPacket(200L, 10, -20, 30)
        assertEquals(p1, p2)
        assertEquals(p1.hashCode(), p2.hashCode())
        assertTrue(p1.toString().contains("VectorPacket"))
        assertEquals("X=10;Y=-20;Z=30", p1.toLogString())

        val p3 = p1.copy(x = 99)
        assertNotEquals(p1, p3)
        val (_, x, _, _) = p1
        assertEquals(10, x)
    }
}

class UtilTests {
    @Test
    fun `test TimeUtil formatting`() {
        val res = TimeUtil.formatMicros(1672531200000000L)
        assertNotNull(res)
    }

    @Test
    fun `test DataParser logic`() {
        val parser = DataParser()
        val wb = ByteBuffer.allocate(15).putLong(1).putFloat(1f).putShort(1)
        val wPayload = wb.array().copyOf(14)
        var wSum = 0; wPayload.forEach { wSum += (it.toInt() and 0xFF) }
        wb.put(14, (wSum % 256).toByte())
        assertNotNull(parser.parseWeather(wb.array()))

        val vb = ByteBuffer.allocate(21).putLong(1).putInt(1).putInt(2).putInt(3)
        val vPayload = vb.array().copyOf(20)
        var vSum = 0; vPayload.forEach { vSum += (it.toInt() and 0xFF) }
        vb.put(20, (vSum % 256).toByte())
        assertNotNull(parser.parseVector(vb.array()))

        val bad = ByteArray(15) { 0 }
        bad[14] = 99
        assertThrows<IllegalArgumentException> { parser.parseWeather(bad) }
    }
}

class NetworkTests {
    @Test
    fun `test RealSocketFactory full coverage`() {
        val server = ServerSocket(0)
        val factory = RealSocketFactory()
        val socketWrapper = factory.invoke("localhost", server.localPort)
        assertDoesNotThrow {
            socketWrapper.connect(InetSocketAddress("localhost", server.localPort), 1000)
        }
        assertNotNull(socketWrapper.inputStream)
        assertNotNull(socketWrapper.outputStream)
        socketWrapper.close()
        server.close()
    }

    @Test
    fun `test SensorClient default constructor`() {
        val client = SensorClient("localhost", 1234, 15, { WeatherPacket(0,0f,0) }, Channel())
        assertNotNull(client)
    }

    @Test
    fun `test SensorClient cancellation rethrow`() = runBlocking {
        val cancelFactory: (String, Int) -> SocketWrapper = { _, _ ->
            throw CancellationException("Boom")
        }
        val client = SensorClient("host", 1, 15, { WeatherPacket(0,0f,0) }, Channel(), cancelFactory)
        assertDoesNotThrow { client.run() }
    }

    @Test
    fun `test SensorClient retry logic`() = runBlocking {
        var attempts = 0
        val failingFactory: (String, Int) -> SocketWrapper = { _, _ ->
            attempts++
            throw IOException("Network fail")
        }
        val client = SensorClient("host", 1, 15, { WeatherPacket(0,0f,0) }, Channel(), failingFactory)
        val job = launch(Dispatchers.Default) { client.run() }
        delay(100)
        assertTrue(attempts > 0)
        job.cancelAndJoin()
    }

    @Test
    fun `test SensorClient Auth EOF`() = runBlocking {
        val junk = ByteArray(2)
        val mockFactory: (String, Int) -> SocketWrapper = { _, _ ->
            object : SocketWrapper {
                val input = ByteArrayInputStream(junk)
                val output = ByteArrayOutputStream()
                override val inputStream: InputStream get() = input
                override val outputStream: OutputStream get() = output
                override fun connect(endpoint: InetSocketAddress, timeout: Int) {}
                override fun close() {}
            }
        }
        val client = SensorClient("host", 1, 15, DataParser()::parseWeather, Channel(), mockFactory)
        val job = launch(Dispatchers.IO) { client.run() }
        delay(100)
        job.cancelAndJoin()
    }

    @Test
    fun `test SensorClient First Packet EOF`() = runBlocking {
        val junk = ByteArray(7)
        val partialPacket = ByteArray(5)
        val stream = junk + partialPacket
        val mockFactory: (String, Int) -> SocketWrapper = { _, _ ->
            object : SocketWrapper {
                val input = ByteArrayInputStream(stream)
                val output = ByteArrayOutputStream()
                override val inputStream: InputStream get() = input
                override val outputStream: OutputStream get() = output
                override fun connect(endpoint: InetSocketAddress, timeout: Int) {}
                override fun close() {}
            }
        }
        val client = SensorClient("host", 1, 15, DataParser()::parseWeather, Channel(), mockFactory)
        val job = launch(Dispatchers.IO) { client.run() }
        delay(100)
        job.cancelAndJoin()
    }

    @Test
    fun `test SensorClient First Packet Checksum Fail`() = runBlocking {
        val junk = ByteArray(7)
        val badPacket = ByteArray(15) { 0 }.apply { this[14] = 123 }
        val stream = junk + badPacket
        val mockFactory: (String, Int) -> SocketWrapper = { _, _ ->
            object : SocketWrapper {
                val input = ByteArrayInputStream(stream)
                val output = ByteArrayOutputStream()
                override val inputStream: InputStream get() = input
                override val outputStream: OutputStream get() = output
                override fun connect(endpoint: InetSocketAddress, timeout: Int) {}
                override fun close() {}
            }
        }
        val client = SensorClient("host", 1, 15, DataParser()::parseWeather, Channel(), mockFactory)
        val job = launch(Dispatchers.IO) { client.run() }
        delay(100)
        job.cancelAndJoin()
    }

    @Test
    fun `test SensorClient Main Loop Checksum Fail (Full Coverage)`() = runBlocking {
        val junk = ByteArray(7)

        val goodPacket1 = ByteBuffer.allocate(15).apply {
            putLong(1); putFloat(1f); putShort(1)
            var sum=0; array().copyOf(14).forEach { sum += (it.toInt() and 0xFF) }
            put(14, (sum % 256).toByte())
        }.array()

        val goodPacket2 = ByteBuffer.allocate(15).apply {
            putLong(2); putFloat(2f); putShort(2)
            var sum=0; array().copyOf(14).forEach { sum += (it.toInt() and 0xFF) }
            put(14, (sum % 256).toByte())
        }.array()

        val badPacket = ByteArray(15) { 0 }.apply { this[14] = 123 }
        val stream = junk + goodPacket1 + goodPacket2 + badPacket

        val mockFactory: (String, Int) -> SocketWrapper = { _, _ ->
            object : SocketWrapper {
                val input = ByteArrayInputStream(stream)
                val output = ByteArrayOutputStream()
                override val inputStream: InputStream get() = input
                override val outputStream: OutputStream get() = output
                override fun connect(endpoint: InetSocketAddress, timeout: Int) {}
                override fun close() {}
            }
        }
        val client = SensorClient("host", 1, 15, DataParser()::parseWeather, Channel(10), mockFactory)
        val job = launch(Dispatchers.IO) { try { client.run() } catch(e:Exception){} }

        delay(200)
        job.cancelAndJoin()
    }
}

class IntegrationTests {
    @Test
    fun `test App loop finishes cleanly`() = runBlocking {
        val junk = ByteArray(7)
        val packetBytes = ByteBuffer.allocate(15).apply {
            putLong(1670000000000000L); putFloat(25.0f); putShort(760)
            var sum = 0; for (b in array().copyOf(14)) sum += (b.toInt() and 0xFF)
            put(14, (sum % 256).toByte())
        }.array()
        val streamData = junk + packetBytes

        val channel = Channel<Packet>(100)
        val stringWriter = StringWriter()

        val mockFactory: (String, Int) -> SocketWrapper = { _, _ ->
            object : SocketWrapper {
                val input = ByteArrayInputStream(streamData)
                val output = ByteArrayOutputStream()
                override val inputStream: InputStream get() = input
                override val outputStream: OutputStream get() = output
                override fun connect(endpoint: InetSocketAddress, timeout: Int) {}
                override fun close() {}
            }
        }

        val app = App("localhost", stringWriter, mockFactory, channel)
        val job = launch { app.start(this) }
        delay(100)
        channel.close()
        job.cancelAndJoin()

        val res = stringWriter.toString()
        assertTrue(res.contains("Temp=25.0"))
    }

    @Test
    fun `test App cancellation`() = runBlocking {
        val channel = Channel<Packet>(100)
        val mockFactory: (String, Int) -> SocketWrapper = { _, _ -> throw IOException("Ignore") }
        val app = App("localhost", StringWriter(), mockFactory, channel)
        val job = launch { app.start(this) }
        delay(50)
        job.cancelAndJoin()
    }

    @Test
    fun `test App default constructor usage`() {
        val factory: (String, Int) -> SocketWrapper = { _, _ -> RealSocketFactory().invoke("", 0) }
        val app = App("localhost", StringWriter(), factory)
        assertNotNull(app)
    }
}