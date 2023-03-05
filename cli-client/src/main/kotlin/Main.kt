import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import java.io.File

fun interact(session: FtpSession) = session.use {
    while (true) {
        val cmd = readLine() ?: run {
            println("EOF found, terminating...")
            return
        }

        val splitCmd = cmd.split("\\s+".toRegex())
        if (splitCmd.isEmpty()) {
            println("Error: empty command")
            continue
        }

        when (splitCmd[0]) {
            "ls" -> {
                if (splitCmd.size > 2) {
                    println("Error: at most 1 argument expected to ls")
                    continue
                }

                session.lsDirs(splitCmd.getOrNull(1) ?: ".").forEach {
                    println("D $it")
                }

                session.lsFiles(splitCmd.getOrNull(1) ?: ".").forEach {
                    println("F $it")
                }
            }

            "download" -> { // download <from> <to>
                if (splitCmd.size != 3) {
                    println("Error: exactly 2 arguments expected to download")
                }
                val fromPath = splitCmd[1]
                val toFile = File(splitCmd[2])
                if (session.download(fromPath, toFile)) {
                    println("Successfully downloaded file!")
                } else {
                    println("Unknown error occurred while downloading file")
                }
            }

            "upload" -> { // upload <from> <to>
                if (splitCmd.size != 3) {
                    println("Error: exactly 2 arguments expected to download")
                }
                val file = File(splitCmd[1])
                val to = splitCmd[2]
                if (session.upload(file, to)) {
                    println("Successfully uploaded file!")
                } else {
                    println("Unknown error occurred while uploading file")
                }
            }

            "finish" -> break

            else -> println("Not a valid command: ${splitCmd[0]}")
        }
    }
}

fun main(args: Array<String>) {
    val parser = ArgParser("cli-client")
    val host by parser.argument(ArgType.String, "host", "Ip address of server to connect to")
    val port by parser.argument(ArgType.Int, "port", "Port to establish connection")
    val username by parser.argument(ArgType.String, "username", "Username used to login")
    val password by parser.option(ArgType.String, "password", "p", "Password used to login")
    parser.parse(args)

    val session = try {
        FtpSession(host, port, username, password)
    } catch (e: FTPConnectionException) {
        println("Error during creating FTP session: ${e.message}")
        return
    }

    println("Successfully created session, waiting for commands:")
    try {
        interact(session)
    } catch (e: Exception) {
        println("Unexpected exception occurred: ${e.message}")
        throw e
    }
}