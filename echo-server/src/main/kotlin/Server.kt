import java.net.*

fun Socket.handleClient() {
    val text = getInputStream().bufferedReader().readLine()

    getOutputStream().bufferedWriter().let {
        it.write(text.uppercase())
        it.flush()
    }
}

fun main() {
    val socket = ServerSocket(config.port, 0, Inet6Address.getByName(config.address))

    while (true) {
        socket.accept().use {
            it.handleClient()
        }
    }
}