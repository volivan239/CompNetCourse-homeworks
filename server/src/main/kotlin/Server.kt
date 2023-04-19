import aliexpresstcp.GoBackNSocket
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

import java.net.InetSocketAddress

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val parser = ArgParser("client")

    val port by parser.argument(ArgType.Int, "port", "Port to run server on")

    parser.parse(args)

    val socket = GoBackNSocket(InetSocketAddress(port))
    while (true) {
        val (query, _) = socket.receiveQuery()

        val file = File(query.fileName)
        file.writeBytes(query.contents)
    }
}