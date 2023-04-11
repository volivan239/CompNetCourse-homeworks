import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureNanoTime

fun main(args: Array<String>) {
    val parser = ArgParser("echo-client")
    val serverHost by parser.argument(ArgType.String, "host", "Server address")
    val serverPort by parser.argument(ArgType.Int, "port", "Server port")
    val timeout by parser.option(ArgType.Int, "timeout", "t", "Timeout for one echo request").default(1000)
    val requestCount by parser.option(ArgType.Int, "count", "c", "Number of echo requests to do").default(10)
    parser.parse(args)

    val socket = DatagramSocket(InetSocketAddress(4000 + Random().nextInt(1000))).also {
        it.soTimeout = timeout
    }

    var totalRtt = 0L
    var minRtt = Long.MAX_VALUE
    var maxRtt = 0L
    var responseCount = 0
    val fmt = SimpleDateFormat("HH:mm:ss.SSS")

    repeat(requestCount) { i ->
        val queryString = "Ping ${i+1} ${fmt.format(Date())}"
        val query = queryString.toByteArray()
        val requestPacket = DatagramPacket(query, query.size, InetSocketAddress(serverHost, serverPort))
        print("$queryString: ")

        try {
            val responsePacket = DatagramPacket(ByteArray(query.size), query.size)
            val rtt = measureNanoTime {
                socket.send(requestPacket)
                socket.receive(responsePacket)
            } / 1000
            
            totalRtt += rtt
            minRtt = minOf(minRtt, rtt)
            maxRtt = maxOf(maxRtt, rtt)
            responseCount += 1
            
            val response = String(responsePacket.data, 0, responsePacket.length)

            println("Response=\"$response\", rtt cur(min/avg/max)=$rtt($minRtt/${totalRtt/responseCount}/$maxRtt) us")
        } catch (_: SocketTimeoutException) {
            println("Request timed out")
        }
    }
    println("Packet loss = ${100 * (requestCount - responseCount) / requestCount}%")
}