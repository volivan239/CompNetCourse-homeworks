import kotlinx.serialization.Serializable
import java.net.InetSocketAddress

enum class Status {
    STARTING,
    ALIVE,
    FINISHING
}

@Serializable
data class StatusNotification(val address: String, val port: Int, val status: Status) {
    constructor(address: InetSocketAddress, status: Status):
            this(address.hostName, address.port, status)

    val inetSocketAddress: InetSocketAddress
        get() = InetSocketAddress(address, port)

    companion object {
        const val MAXSIZE = 128
    }
}