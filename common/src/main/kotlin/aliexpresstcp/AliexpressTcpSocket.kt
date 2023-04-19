package aliexpresstcp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import mu.KotlinLogging
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
class AliexpressTcpSocket(
    socketAddress: InetSocketAddress,
    private val connectionConfig: ConnectionConfig = defaultConfig
): AutoCloseable {

    private val udpSocket = DatagramSocket(socketAddress)
    private val rng = Random(239)
    private val logger = KotlinLogging.logger {}

    override fun close() {
        udpSocket.close()
    }

    private fun sendUnchecked(packet: AliexpressTCPPacket, receiverAddress: InetSocketAddress) {
        // Imitating network losses
        logger.info("Sending $packet to $receiverAddress...")
        if (rng.nextFloat() < connectionConfig.dropRate) {
            logger.info("Lost simulated")
            return
        }

        logger.info("Successfully sent")
        val bytes = ProtoBuf.encodeToByteArray(packet)
        udpSocket.send(DatagramPacket(bytes, bytes.size, receiverAddress))
    }

    private fun receiveUnchecked(what: String): Pair<AliexpressTCPPacket, InetSocketAddress>? {
        val buf = ByteArray(connectionConfig.maxDataSize + 1024)
        val packet = DatagramPacket(buf, buf.size)
        logger.info("Waiting for $what...")

        try {
            udpSocket.receive(packet)
        } catch (_: SocketTimeoutException) {
            if (connectionConfig.enableLogging) {
                logger.warn("Receive timed out")
            }
            return null
        }

        val aliexpressPacket = ProtoBuf.decodeFromByteArray<AliexpressTCPPacket>(packet.data.copyOf(packet.length))
        val senderAddress = packet.socketAddress as InetSocketAddress

        logger.info("Received $aliexpressPacket from $senderAddress")
        return aliexpressPacket to senderAddress
    }

    /**
     * @return true if all the data sent was successfully received, or false if something went wrong
     */
    fun receiveData(output: OutputStream): InetSocketAddress? {
        udpSocket.soTimeout = 0 // Will be applied only until first successful receive

        var num = 0
        var receivedFin = false
        var result: InetSocketAddress? = null

        while (true) {
            val (packet, senderAddress) = receiveUnchecked("incoming data") ?: break
            result = senderAddress

            if (packet.num > num || receivedFin && !packet.fin) {
                // Unexpected packet, ignoring
                continue
            }

            udpSocket.soTimeout = connectionConfig.receiverTimeoutMillis
            sendUnchecked(AliexpressTCPPacket.getAckFor(packet), senderAddress)

            if (packet.num < num) {
                // Duplicate detected
                continue
            }

            // Received new packet

            output.write(packet.data)
            num++

            if (packet.fin) {
                receivedFin = true
            }
        }

        return if (receivedFin) {
            result
        } else {
            null
        }
    }

    fun receiveAll(): Pair<ByteArray, InetSocketAddress>? {
        val bytes = ByteArrayOutputStream()
        return receiveData(bytes)?.let {
            return bytes.toByteArray() to it
        }
    }

    /**
     * Sends all [data] and ensures that it is received by the other side.
     * @return true if message is successfully sent, false if something went wrong
     */
    fun sendData(data: InputStream, receiverAddress: InetSocketAddress): Boolean {
        udpSocket.soTimeout = connectionConfig.ackTimeoutMillis

        var num = 0
        val bytes = ByteArray(connectionConfig.maxDataSize)

        while (true) {
            val lengthRead = data.readNBytes(bytes, 0, connectionConfig.maxDataSize)

            if (lengthRead == 0) {
                break
            }

            val packet = AliexpressTCPPacket(num, ack = false, fin = false, bytes.copyOf(lengthRead))
            do {
                sendUnchecked(packet, receiverAddress)
            } while (receiveUnchecked("ack")?.first?.isAckFor(packet) != true)

            num++
        }

        val finPacket = AliexpressTCPPacket(num, ack = false, fin = true, byteArrayOf())
        repeat(connectionConfig.senderMaxFinCount) {
            sendUnchecked(finPacket, receiverAddress)
            if (receiveUnchecked("fin ack")?.first?.isAckFor(finPacket) == true) {
                return true
            }
        }

        return false
    }

    fun sendData(data: ByteArray, receiverAddress: InetSocketAddress): Boolean = sendData(data.inputStream(), receiverAddress)
}