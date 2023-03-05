import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import rawhttp.core.*
import java.io.File
import java.io.IOException
import java.net.Socket
import java.net.URI
import java.nio.charset.Charset

fun handleResponse(fileName: String, response: RawHttpResponse<*>) {
    println("Raw response:\n${response}\n\n")

    if (response.statusCode != 200) {
        println("Server returned ${response.statusCode}")
        return
    }

    val contentType = response.headers["Content-type"][0]
    val body = response.body.get()

    if (contentType.startsWith("text")) {
        println("Successfully received $fileName:\n${body.asRawString(Charset.defaultCharset())}")
    } else {
        try {
            val file = File(fileName)
            file.writeBytes(body.asRawBytes())
            println("Successfully saved file to $fileName")
        } catch (e: IOException) {
            println("Failed to save file")
        }
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("server")
    val host by parser.argument(ArgType.String, "host", "Ip address of host to connect to")
    val port by parser.argument(ArgType.Int, "port", "Port to establish connection")
    val file by parser.argument(ArgType.String, "file", "File to download")
    parser.parse(args)

    val socket = try {
        Socket(host, port)
    } catch (e: Exception) {
        println("Error while opening TCP connection: ${e.message}")
        return
    }

    val requestLine = RequestLine("GET", URI("/$file"), HttpVersion.HTTP_1_1)
    val headers = RawHttpHeaders.newBuilder().with("Host", "$host:$port").build()
    val request = RawHttpRequest(requestLine, headers, null, null)

    val response = socket.use {
        request.writeTo(it.getOutputStream())
        RawHttp().parseResponse(it.getInputStream()).eagerly()
    }

    handleResponse(file, response)
}