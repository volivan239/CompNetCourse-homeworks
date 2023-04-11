import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.*

fun main(args: Array<String>) {
    val parser = ArgParser("heartbeat-client")
    val socket = DatagramSocket(InetSocketAddress(4000 + Random().nextInt(1000)))
    val serverHost by parser.argument(ArgType.String, "host", "Server address")
    val serverPort by parser.argument(ArgType.Int, "port", "Server port")
    parser.parse(args)

    var num = 0L
    while (true) {
        num += 1
        val buffer = ByteBuffer.allocate(2 * Long.SIZE_BYTES)
        buffer.putLong(num)
        buffer.putLong(Date().time)
        val requestPacket = DatagramPacket(buffer.array(), buffer.capacity(), InetSocketAddress(serverHost, serverPort))
        socket.send(requestPacket)
        sleep(1000L)
    }
}