import aliexpresstcp.AliexpressTcpSocket
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.io.IOException

import java.net.InetSocketAddress

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val socket = AliexpressTcpSocket(InetSocketAddress("localhost", 3456))
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