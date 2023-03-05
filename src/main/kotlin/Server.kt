import rawhttp.core.RawHttp
import rawhttp.core.RequestLine
import java.net.ServerSocket
import java.net.Socket
import java.net.URI
import java.net.URISyntaxException
import kotlin.concurrent.thread

fun handleClient(socket: Socket) = socket.use {
    val request = try {
        RawHttp().parseRequest(it.getInputStream())
    } catch (_: Exception) {
        Responder.invalidURLResponse.writeTo(it.getOutputStream())
        return@use
    }

    if (request.method != "GET" && request.method != "POST") {
        Responder.methodNotAllowedResponse.writeTo(it.getOutputStream())
        return@use
    }

    val realRequest = request.uri.path.drop(1)
    if (config.banned.any { realRequest.startsWith(it) }) {
        Responder.blockedPageResponse.writeTo(it.getOutputStream())
        return@use
    }

    val redirectedURL = try {
        URI("http://$realRequest")
    } catch (_: URISyntaxException) {
        Responder.invalidURLResponse.writeTo(it.getOutputStream())
        return@use
    }

    val newRequestLine = RequestLine(request.method, redirectedURL, request.startLine.httpVersion)
    val newRequest = request.withRequestLine(newRequestLine)

    val response = RequestSender.sendRequestWithCacheCheck(newRequest)
    response.writeTo(it.getOutputStream())
}

fun main() {
    val server = ServerSocket(config.port)

    while (true) {
        val socket = server.accept()
        thread {
            handleClient(socket)
        }
    }
}