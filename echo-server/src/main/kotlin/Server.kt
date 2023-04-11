import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import kotlin.random.Random

fun main() {
    val socket = DatagramSocket(InetSocketAddress(config.port))

    val requestPacket = DatagramPacket(ByteArray(config.maxsize), config.maxsize)
    val rnd = Random(0)

    while (true) {
        socket.receive(requestPacket)

        if (rnd.nextDouble() < config.droprate) {
            continue
        }

        val request = String(requestPacket.data, 0, requestPacket.length)
        val response = request.uppercase().toByteArray()
        val responsePacket = DatagramPacket(response, response.size, requestPacket.socketAddress)
        socket.send(responsePacket)
    }
}