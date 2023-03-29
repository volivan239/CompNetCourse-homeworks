import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*


fun main() {
    val socket = DatagramSocket(null)

    while (true) {
        Thread.sleep(1000)
        val data = Date().toString().toByteArray()
        socket.send(DatagramPacket(data, data.size, InetAddress.getByName(config.broadcastip), config.port))
    }
}