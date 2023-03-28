import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

fun handleClient(socket: Socket) = socket.use {
    val cmd = it
        .getInputStream()
        .bufferedReader()
        .readLine()
        .split(" ")

    val process = ProcessBuilder(*cmd.toTypedArray())
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start()

    process.waitFor(config.cmdtime, TimeUnit.SECONDS)
    process.inputStream.transferTo(socket.getOutputStream())
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