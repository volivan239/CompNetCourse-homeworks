import java.net.Socket

fun main(args: Array<String>) {
    val host = args[0]
    val port = args[1].toInt()
    val cmd = args[2]

    val socket = Socket(host, port)
    with (socket.getOutputStream().bufferedWriter()) {
        write("$cmd\n")
        flush()
    }

    val response = socket.getInputStream().bufferedReader().readText()
    println(response)
}