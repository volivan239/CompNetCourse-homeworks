import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@OptIn(ExperimentalSerializationApi::class)
class NotificationsMonitor {
    private val lastNotification = ConcurrentHashMap<InetSocketAddress, Int>()

    @Volatile
    var isRunning = false

    @Volatile
    var timeoutMillis = defaultTimeoutMillis

    @Volatile
    var patience = defaultPatience

    val aliveAddresses: List<String>
        get() = lastNotification.keys.map { "${it.address.hostAddress}:${it.port}" }

    private fun DatagramSocket.sendStatusNotification(address: InetSocketAddress, notification: StatusNotification) {
        val notificationBuf = ProtoBuf.encodeToByteArray(notification)

        send(DatagramPacket(notificationBuf, notificationBuf.size, address))
    }

    private fun DatagramSocket.receiveStatusNotification(timeoutMillis: Int): StatusNotification? {
        soTimeout = timeoutMillis

        val buf = ByteArray(StatusNotification.MAXSIZE)
        val packet = DatagramPacket(buf, buf.size)
        return try {
            receive(packet)
            ProtoBuf.decodeFromByteArray<StatusNotification>(packet.data.copyOf(packet.length)).also {
                println(it)
            }
        } catch (_: SocketTimeoutException) {
            null
        }
    }

    fun run() {
        isRunning = true

        val interfaceAddress = NetworkInterface.getNetworkInterfaces()
            .asSequence()
            .filterNot { it.isLoopback }
            .flatMap { it.interfaceAddresses }
            .first()

        val socket = DatagramSocket(InetSocketAddress(interfaceAddress.address, 0))
        socket.broadcast = true

        val broadcastListenerSocket = DatagramSocket(null)
        broadcastListenerSocket.reuseAddress = true
        broadcastListenerSocket.bind(InetSocketAddress(broadcastPort))

        val selfAddress = InetSocketAddress(interfaceAddress.address, socket.localPort)
        val broadcastAddress = InetSocketAddress(interfaceAddress.broadcast, broadcastPort)

        socket.sendStatusNotification(broadcastAddress, StatusNotification(selfAddress, Status.STARTING))

        var lastUpdate = Date().time
        while (isRunning) { // Waiting for answers to initial status notification
            val curTime = Date().time
            val timeLeft = timeoutMillis - (curTime - lastUpdate).toInt()

            if (timeLeft < 0) {
                break
            }

            val notification = socket.receiveStatusNotification(timeLeft) ?: continue
            lastNotification[notification.inetSocketAddress] = 0
        }

        var iterCount = 0
        while (isRunning) {
            val curTime = Date().time
            val timeLeft = timeoutMillis - (curTime - lastUpdate).toInt()

            if (timeLeft <= 0) {
                socket.sendStatusNotification(broadcastAddress, StatusNotification(selfAddress, Status.ALIVE))

                val deadInstances = lastNotification.filterValues { iterCount - it > patience }.keys
                deadInstances.forEach {
                    lastNotification.remove(it)
                }

                iterCount++
                lastUpdate = curTime
                continue
            }

            val notification = broadcastListenerSocket.receiveStatusNotification(timeLeft) ?: continue
            when (notification.status) {
                Status.STARTING -> {
                    lastNotification[notification.inetSocketAddress] = iterCount
                    socket.sendStatusNotification(
                        notification.inetSocketAddress,
                        StatusNotification(selfAddress, Status.ALIVE)
                    )
                }

                Status.ALIVE -> lastNotification[notification.inetSocketAddress] = iterCount
                Status.FINISHING -> lastNotification.remove(notification.inetSocketAddress)
            }
        }

        socket.sendStatusNotification(broadcastAddress, StatusNotification(selfAddress, Status.FINISHING))
    }

    companion object {
        const val defaultPatience = 3
        const val broadcastPort = 4447
        const val defaultTimeoutMillis = 2000
    }
}