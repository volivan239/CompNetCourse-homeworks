import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import java.net.*

fun main(args: Array<String>) {
    val parser = ArgParser("echo-client")
    val serverHost by parser.argument(ArgType.String, "host", "Server address")
    val serverPort by parser.argument(ArgType.Int, "port", "Server port")
    val message by parser.option(ArgType.String, "message", "Echo message to send").default("Echo hello")
    parser.parse(args)

    val socket = Socket(Inet6Address.getByName(serverHost), serverPort)
    val response = socket.use {
        socket.getOutputStream().bufferedWriter().let {
            it.write("$message\n")
            it.flush()
        }
        socket.getInputStream().bufferedReader().readText()
    }

    println("Successfully received \"$response\"!")
}