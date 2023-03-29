import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress

fun main(args: Array<String>) {
    val port = args[0].toInt()

    val socket = DatagramSocket(null)
    socket.broadcast = true
    socket.reuseAddress = true
    socket.bind(InetSocketAddress(port))

    val buf = ByteArray(1024)
    val packet = DatagramPacket(buf, 1024)

    while (true) {
        socket.receive(packet)
        println(String(buf.take(packet.length).toByteArray()))
    }
}