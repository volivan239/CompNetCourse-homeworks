import aliexpresstcp.GoBackNSocket
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.net.InetSocketAddress
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val parser = ArgParser("client")

    val serverHost by parser.argument(ArgType.String, "host", "Server address")
    val serverPort by parser.argument(ArgType.Int, "port", "Server port")
    val fileName by parser.argument(ArgType.String, "filename", "Name of file to transmit")
    parser.parse(args)

    val socket = GoBackNSocket(InetSocketAddress(4000 + Random.nextInt(1000)))
    val serverAddress = InetSocketAddress(serverHost, serverPort)

    val query = Query(fileName, File(fileName).readBytes())
    socket.sendQuery(query, serverAddress)

    println("Successfully sent file" )
}