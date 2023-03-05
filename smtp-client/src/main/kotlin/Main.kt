import clients.LibraryBasedSMTPClient
import clients.RawSMTPClient
import java.io.File

fun main(args: Array<String>) {
    if (args.size < 3) {
        println("Error parsing arguments. Format: 'client <receiver> <subject> <file with text> [<attachment files>]'")
        return
    }

    val receiver = args[0]
    val subject = args[1]
    val files = args.toList().drop(2).map { fileName ->
        File(fileName).also {
            if (!it.isFile) {
                println("Can't find file $fileName")
                return@main
            }
        }
    }

    val client = when (config.client) {
        Client.Raw -> RawSMTPClient(config)
        Client.LibraryBased -> LibraryBasedSMTPClient(config)
    }

    try {
        client.sendMessage(receiver, subject, files[0], files.drop(1))
        println("Message sent successfully!")
    } catch (e: Exception) {
        println("Can't send message: ${e.message}")
        if (config.debug) {
            e.printStackTrace()
        }
    }
}