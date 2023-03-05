package clients

import Config
import java.io.BufferedReader
import java.io.File
import java.io.PrintStream
import java.net.InetAddress
import java.net.Socket
import java.nio.file.Files
import java.util.*
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

class RawSMTPClient(config: Config) : AbstractSMTPClient(config) {
    override fun sendMessage(receiver: String, subject: String, messageFile: File, attachments: List<File>) {
        val messageContent = buildMessageHeadersAndBody(receiver, subject, messageFile, attachments)

        val preSSLSocket = Socket(config.host, config.port)
        val preSSLOut = PrintStream(preSSLSocket.outputStream.buffered())
        val preSSLIn = preSSLSocket.inputStream.bufferedReader()

        try {
            sendMessageAndCheckResponse(preSSLOut, preSSLIn, null, SMTPCodes.serviceReady)
            sendMessageAndCheckResponse(preSSLOut, preSSLIn, ehloQuery, SMTPCodes.okay, true)
            sendMessageAndCheckResponse(preSSLOut, preSSLIn, startTLS, SMTPCodes.serviceReady)
        } catch (e: Exception) {
            preSSLIn.close()
            preSSLOut.close()
            throw e
        }

        val socket = (SSLSocketFactory.getDefault() as (SSLSocketFactory))
            .createSocket(preSSLSocket, config.host, config.port, true) as SSLSocket
        socket.enabledProtocols = enabledProtocols
        socket.enabledCipherSuites = enabledCipherSuites

        PrintStream(socket.outputStream.buffered()).use { out ->
            socket.inputStream.bufferedReader().use { inp ->
                sendMessageAndCheckResponse(out, inp, ehloQuery, SMTPCodes.okay, true)
                sendMessageAndCheckResponse(out, inp, authLoginQuery, SMTPCodes.serverChallenge)
                sendMessageAndCheckResponse(out, inp, authLoginEncoded, SMTPCodes.serverChallenge)
                sendMessageAndCheckResponse(out, inp, authPassEncoded, SMTPCodes.authSucceeded)
                sendMessageAndCheckResponse(out, inp, mailFromQuery, SMTPCodes.okay)
                sendMessageAndCheckResponse(out, inp, mailToQuery(receiver), SMTPCodes.okay)
                sendMessageAndCheckResponse(out, inp, dataQuery, SMTPCodes.startMail)
                sendMessageAndCheckResponse(out, inp, messageContent, SMTPCodes.okay)
                sendMessageAndCheckResponse(out, inp, quitQuery, SMTPCodes.goodBye)
            }
        }
    }

    private fun sendMessageAndCheckResponse(out: PrintStream, inp: BufferedReader, message: String?, expectedCode: Int?, multiLineAnswer: Boolean = false) {
        if (message != null) {
            if (config.debug) {
                println("Q: $message")
            }

            out.println(message)
            out.flush()
        }

        val response = if (multiLineAnswer) {
            CharArray(maxResponseSize).let { it.take(inp.read(it)).joinToString(separator="") }
        } else {
            inp.readLine()
        }

        if (config.debug) {
            println("R: $response")
        }

        if (expectedCode != null) {
            val actualCode = response.take(3).toIntOrNull()
            if (actualCode != expectedCode) {
                throw IllegalStateException("Server returned unexpected code $actualCode (expected $expectedCode)")
            }
        }
    }

    private fun buildMessageHeadersAndBody(receiver: String, subject: String, messageFile: File, attachments: List<File>): String {
        return buildString {
            appendLine(buildMainHeaders(receiver, subject))
            appendLine("--$mimeDelimiter")
            append(buildPartWithDelimiter(messageFile, isAttachment = false, isFinal = attachments.isEmpty()))
            attachments.forEachIndexed { index, file ->
                append(buildPartWithDelimiter(file, isAttachment = true, isFinal = index == attachments.size - 1))
            }
            appendLine(".")
        }.replace("\r\n", "\n").replace("\n", "\r\n")
        // First replacement prevents changing \r\n to \r\r\n
    }

    private fun buildMainHeaders(receiver: String, subject: String): String {
        return """
        From: ${config.username}
        To: $receiver
        Date: ${Date()}
        Subject: $subject
        MIME-Version: 1.0
        Content-Type: multipart/mixed; boundary="$mimeDelimiter"
    """.trimIndent().plus("\n")
    }

    private fun buildPartWithDelimiter(file: File, isAttachment: Boolean, isFinal: Boolean): String {
        val encoding: String
        val body: String
        if (file.contentType.startsWith("text")) {
            encoding = "7bit"
            body = file.readText()
        } else {
            encoding = "base64"
            body = Base64.getMimeEncoder().encodeToString(file.readBytes())
        }

        return buildString {
            appendLine("Content-Type: ${file.contentType}")
            appendLine("Content-Transfer-Encoding: $encoding")
            if (isAttachment) {
                appendLine("Content-Disposition: attachment; filename=${file.name}")
            }
            appendLine()

            appendLine(body)
            val delimiter = if (isFinal) "--$mimeDelimiter--" else "--$mimeDelimiter"
            appendLine(delimiter)
        }
    }

    private val authLoginEncoded: String
        get() = Base64.getEncoder().encodeToString(config.username.toByteArray())

    private val authPassEncoded: String
        get() = Base64.getEncoder().encodeToString(config.password.toByteArray())

    private val mailFromQuery: String
        get() = "MAIL FROM:<${config.username}>"

    private fun mailToQuery(receiver: String): String =
        "RCPT TO:<$receiver>"

    private object SMTPCodes {
        const val serviceReady = 220
        const val okay = 250
        const val goodBye = 221
        const val serverChallenge = 334
        const val startMail = 354
        const val authSucceeded = 235
    }

    private val File.contentType
        get() = Files.probeContentType(toPath()) ?: "text/plain"

    companion object {
        private val enabledProtocols = arrayOf("TLSv1.3")
        private val enabledCipherSuites = arrayOf("TLS_AES_128_GCM_SHA256")

        private const val startTLS = "STARTTLS"
        private const val authLoginQuery = "AUTH LOGIN"
        private const val dataQuery = "DATA"
        private const val quitQuery = "QUIT"

        private val mimeDelimiter = "Delimiter${Random().nextLong()}"

        private const val maxResponseSize = 1024

        private val ehloQuery: String
            get() = "EHLO ${InetAddress.getLocalHost().hostAddress}"
    }
}