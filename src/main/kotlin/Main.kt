import rawhttp.core.RawHttp
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

fun handleClient(socket: Socket) = socket.use {
    val request = RawHttp().parseRequest(it.getInputStream())

    if (request.method != "GET") {
        Responder.badRequestResponse.writeTo(it.getOutputStream())
        return@use
    }

    val uri = "data" + request.uri.path
    val file = File(uri)
    if (!file.isFile) {
        Responder.notFoundResponse.writeTo(it.getOutputStream())
        return@use
    }

    Responder.getOkResponse(file).writeTo(it.getOutputStream())
}

fun main() {
    val server = ServerSocket(8080)

    while (true) {
        val socket = server.accept()
        thread {
            handleClient(socket)
        }
    }
}