package clients

import Config
import java.io.File
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

class LibraryBasedSMTPClient(config: Config) : AbstractSMTPClient(config) {
    override fun sendMessage(receiver: String, subject: String, messageFile: File, attachments: List<File>) {
        val session = Session.getDefaultInstance(config.smtpProps, config.authenticator)

        session.debug = config.debug

        val message = with (MimeMessage(session)) {
            setFrom(config.username)
            setRecipients(Message.RecipientType.TO, receiver)
            setSubject(subject)
            setContent(createContent(messageFile, attachments))
            return@with this
        }

        session.getTransport("smtp").use {
            it.connect()
            it.sendMessage(message, message.allRecipients)
        }
    }

    private fun createContent(messageFile: File, attachments: List<File>): MimeMultipart {
        val messageText = messageFile.readText()
        val subtype = if (messageFile.extension == "html") "html" else "plain"

        val messagePart = MimeBodyPart()
        messagePart.setText(messageText, null, subtype)

        val attachmentParts = attachments.map {
            val part = MimeBodyPart()
            part.attachFile(it)
            part
        }

        val content = MimeMultipart()
        content.addBodyPart(messagePart)
        attachmentParts.forEach { content.addBodyPart(it) }
        return content
    }

    private val Config.smtpProps: Properties
        get() {
            val props = Properties()
            props.setProperty("mail.smtp.host", host)
            props.setProperty("mail.smtp.port", port.toString())
            props.setProperty("mail.smtp.auth", auth.toString())
            props.setProperty("mail.smtp.starttls.enable", tls.toString())
            return props
        }

    private val Config.authenticator: Authenticator
        get() = object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        }
}