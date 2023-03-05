import kotlinx.cli.*
import rawhttp.core.RawHttp
import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.Executors

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

fun main(args: Array<String>) {
    val parser = ArgParser("server")
    val concurrencyLevel by parser.option(
        ArgType.Int,
        "concurrencyLevel",
        "c",
        "Maximum number of simultaneously handled requests"
    ).default(5)
    parser.parse(args)

    val server = ServerSocket(8080)
    val executor = Executors.newFixedThreadPool(concurrencyLevel)

    while (true) {
        val socket = server.accept()
        executor.submit {
            handleClient(socket)
        }
    }
}