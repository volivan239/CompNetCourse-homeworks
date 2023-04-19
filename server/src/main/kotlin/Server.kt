import aliexpresstcp.AliexpressTcpSocket
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.io.IOException

import java.net.InetSocketAddress

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val parser = ArgParser("client")

    val port by parser.argument(ArgType.Int, "port", "Port to run server on")

    parser.parse(args)

    val socket = AliexpressTcpSocket(InetSocketAddress(port))
    while (true) {
        val (query, clientAddress) = socket.receiveQuery() ?: continue

        when (query.type) {
            QueryType.GET -> {
                val file = File(query.fileName)
                val response = if (!file.exists()) {
                    Response(false, byteArrayOf())
                } else {
                    Response(true, file.readBytes())
                }

                socket.sendResponse(response, clientAddress)
            }
            QueryType.UPLOAD -> {
                val file = File(query.fileName)
                val response = try {
                    file.writeBytes(query.contents)
                    Response(success = true, byteArrayOf())
                } catch (_: IOException) {
                    Response(success = false, byteArrayOf())
                }

                socket.sendResponse(response, clientAddress)
            }
        }
    }
}