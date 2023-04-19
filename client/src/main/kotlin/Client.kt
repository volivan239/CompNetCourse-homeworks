import aliexpresstcp.AliexpressTcpSocket
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File
import java.net.InetSocketAddress
import kotlin.random.Random

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val parser = ArgParser("client")

    val serverHost by parser.argument(ArgType.String, "host", "Server address")
    val serverPort by parser.argument(ArgType.Int, "port", "Server port")
    val queryType by parser.argument(ArgType.Choice<QueryType>(), "query", "query type")
    val fileName by parser.argument(ArgType.String, "filename", "Name of file to transmit")
    parser.parse(args)

    val socket = AliexpressTcpSocket(InetSocketAddress(4000 + Random.nextInt(1000)))
    val serverAddress = InetSocketAddress(serverHost, serverPort)

    when (queryType) {
        QueryType.GET -> {
            val query = Query(queryType, fileName, byteArrayOf())
            val response = socket.sendQuery(query, serverAddress)

            if (response == null) {
                println("Error while sending query")
                return
            }

            if (response.success) {
                val file = File(fileName)
                file.writeBytes(response.data)
                println("Successfully received file")
            } else {
                println("Fail on server side")
            }
        }

        QueryType.UPLOAD -> {
            val query = Query(queryType, fileName, File(fileName).readBytes())
            val response = socket.sendQuery(query, serverAddress)

            if (response == null) {
                println("Error while sending query")
                return
            }

            if (response.success) {
                println("Successfully sent file")
            } else {
                println("Error on server side")
            }
        }
    }
}