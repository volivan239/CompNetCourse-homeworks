import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.File

class FtpSession(
    host: String,
    port: Int,
    user: String,
    password: String?
) : AutoCloseable {
    private val client: FTPClient = FTPClient()

    init {
        client.connect(host, port)

        if (!FTPReply.isPositiveCompletion(client.replyCode)) {
            client.disconnect()
            throw FTPConnectionException("Failed to connect to server")
        }

        if (!client.login(user, password)) {
            throw FTPConnectionException("Failed to login: server returned code ${client.replyCode}")
        }
    }

    fun lsFiles(directory: String = ".") = client.listFiles(directory).filter { it.isFile }.map { it.name }

    fun lsDirs(directory: String = ".") = client.listDirectories(directory).map { it.name }

    fun delete(pathName: String): Boolean = client.deleteFile(pathName)

    fun upload(file: File, to: String): Boolean {
        return file.inputStream().use {
            client.storeFile(to, it)
        }
    }

    fun download(from: String, to: File): Boolean {
        return to.outputStream().use {
            client.retrieveFile(from, it)
        }
    }

    override fun close() {
        client.disconnect()
    }
}

class FTPConnectionException(msg: String): IllegalArgumentException(msg)