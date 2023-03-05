import rawhttp.core.RawHttp
import java.io.File
import java.net.ServerSocket

fun main() {
    val rawHttp = RawHttp()
    val server = ServerSocket(8080)

    while (true) {
        server.accept().use { socket ->
            val request = rawHttp.parseRequest(socket.getInputStream())

            if (request.method != "GET") {
                Responder.badRequestResponse.writeTo(socket.getOutputStream())
                return@use
            }

            val uri = "data" + request.uri.path
            val file = File(uri)
            if (!file.isFile) {
                Responder.notFoundResponse.writeTo(socket.getOutputStream())
                return@use
            }

            Responder.getOkResponse(file).writeTo(socket.getOutputStream())
        }
    }
}