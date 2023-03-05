package clients

import Config
import java.io.File

abstract class AbstractSMTPClient(protected val config: Config) {
    abstract fun sendMessage(receiver: String, subject: String, messageFile: File, attachments: List<File>)
}