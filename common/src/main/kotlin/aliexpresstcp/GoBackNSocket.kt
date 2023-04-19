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
import java.util.*
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
class GoBackNSocket(
    socketAddress: InetSocketAddress,
    private val connectionConfig: ConnectionConfig = defaultConfig
): AutoCloseable {

    private val udpSocket = DatagramSocket(socketAddress)
    private val rng = Random(239)
    private val logger = KotlinLogging.logger {}

    override fun close() {
        udpSocket.close()
    }

    private fun sendPacket(packet: GoBackNPacket, receiverAddress: InetSocketAddress) {
        val msgStart = "Sending $packet to $receiverAddress... "
        val rnd = rng.nextFloat()

        // Imitating network losses
        if (rnd < connectionConfig.dropRate) {
            logger.info(msgStart + "Lost simulated")
            return
        }

        val bytes = ProtoBuf.encodeToByteArray(packet)
        udpSocket.send(DatagramPacket(bytes, bytes.size, receiverAddress))
        logger.info(msgStart  +"Successfully sent")
    }

    private fun receivePacket(what: String): Pair<GoBackNPacket, InetSocketAddress>? {
        val buf = ByteArray(connectionConfig.maxDataSize + 1024)
        val packet = DatagramPacket(buf, buf.size)
        val msgStart = "Waiting for $what... "

        val goBackNPacket = try {
            udpSocket.receive(packet)
            ProtoBuf.decodeFromByteArray<GoBackNPacket>(packet.data.copyOf(packet.length))
        } catch (_: SocketTimeoutException) {
            logger.warn(msgStart + "Timeout exceeded")
            return null
        }

        val senderAddress = packet.socketAddress as InetSocketAddress

        logger.info(msgStart + "Received $goBackNPacket from $senderAddress")
        return goBackNPacket to senderAddress
    }

    fun receiveData(output: OutputStream): InetSocketAddress {
        udpSocket.soTimeout = 0
        var receivedFin = false
        var expectedSeqNum = 0
        lateinit var senderAddress: InetSocketAddress

        do {
            logger.info { "Status: received packets with #[0..$expectedSeqNum), waiting for packet #$expectedSeqNum" }
            val (packet, address) = receivePacket("data") ?: continue
            senderAddress = address
            if (packet.num == expectedSeqNum) {
                output.write(packet.data)
                expectedSeqNum++
                if (packet.fin) {
                    receivedFin = true
                }
            }
            sendPacket(GoBackNPacket(expectedSeqNum - 1, receivedFin, true, byteArrayOf()), address)
        } while (!receivedFin)

        logger.info { "Status: Received all $expectedSeqNum packets, waiting for packets that may be resent" }

        udpSocket.soTimeout = 4 * connectionConfig.timeoutMillis
        while (true) {
            val (_, address) = receivePacket("resent data") ?: break
            sendPacket(GoBackNPacket(expectedSeqNum - 1, true, true, byteArrayOf()), address)
        }

        logger.info { "Status: Finished waiting for resent data" }

        return senderAddress
    }

    fun receiveAll(): Pair<ByteArray, InetSocketAddress> {
        val bytes = ByteArrayOutputStream()
        return receiveData(bytes).let {
            bytes.toByteArray() to it
        }
    }

    fun sendData(data: InputStream, receiverAddress: InetSocketAddress) {
        var base = 0
        var nextSeqNum = 0
        var baseSendTime = 0L
        var hasMoreData = true

        val bytes = ByteArray(connectionConfig.maxDataSize)
        val packets = mutableListOf<GoBackNPacket>()

        while (hasMoreData || base < nextSeqNum) {
            logger.info { "Status: sent and confirmed packets #[0..$base), sent and didn't receive confirmation for packets #[$base, $nextSeqNum)" }

            if (hasMoreData && nextSeqNum < base + connectionConfig.windowSize) {
                val lengthRead = data.readNBytes(bytes, 0, connectionConfig.maxDataSize)

                val packet = if (lengthRead == 0) {
                    hasMoreData = false
                    GoBackNPacket(nextSeqNum, true, false, byteArrayOf())
                } else {
                    GoBackNPacket(nextSeqNum, false, false, bytes.copyOf(lengthRead))
                }

                packets.add(packet)
                sendPacket(packet, receiverAddress)
                if (nextSeqNum == base) {
                    baseSendTime = Date().time
                }
                nextSeqNum++
                continue
            }

            val timeLeft = baseSendTime + connectionConfig.timeoutMillis - Date().time
            if (timeLeft <= 0) {
                for (i in base until nextSeqNum) {
                    sendPacket(packets[i], receiverAddress)
                }
                baseSendTime = Date().time
            } else {
                udpSocket.soTimeout = timeLeft.toInt()
                val (packet, _) = receivePacket("Ack") ?: continue
                if (packet.num >= base) {
                    base = packet.num + 1
                    baseSendTime = Date().time
                }
            }
        }
        logger.info { "Status: sent and confirmed packets #[0..$base), everything was sent!" }
    }

    fun sendData(data: ByteArray, receiverAddress: InetSocketAddress) = sendData(data.inputStream(), receiverAddress)
}