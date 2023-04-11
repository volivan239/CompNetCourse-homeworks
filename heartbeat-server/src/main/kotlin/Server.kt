import java.lang.Thread.sleep
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.nio.ByteBuffer
import java.util.*
import kotlin.concurrent.thread

data class Message(val num: Long, val time: Long)

class ClientsStorage {
    private val lastMessageMap = mutableMapOf<SocketAddress, Message>()

    fun handleMessage(address: InetSocketAddress, message: Message) {
        val timeDiff = Date().time - message.time

        synchronized(lastMessageMap) {
            val numDiff = message.num - (lastMessageMap[address]?.num ?: 0)

            println("Received message #${message.num} from $address, ${numDiff - 1} packets lost, signal time = $timeDiff")

            if (!lastMessageMap.contains(address)) {

                thread {
                    var lastMessage = message
                    while (true) {
                        sleep(lastMessage.time + config.timelimit - Date().time)
                        val newLastMessage = synchronized(lastMessageMap) {
                            lastMessageMap.getValue(address)
                        }
                        if (newLastMessage.num == lastMessage.num) {
                            println("No messages from $address, client is marked as down")
                            synchronized(lastMessageMap) {
                                lastMessageMap.remove(address)
                            }
                            break
                        }
                        lastMessage = newLastMessage
                    }
                }

            }

            lastMessageMap[address] = message
        }
    }
}

fun main() {
    val socket = DatagramSocket(InetSocketAddress(config.port))
    val bufSize = 2 * Long.SIZE_BYTES
    val requestPacket = DatagramPacket(ByteArray(bufSize), bufSize)
    val storage = ClientsStorage()

    while (true) {
        socket.receive(requestPacket)

        val buffer = ByteBuffer.allocate(bufSize)
        buffer.put(requestPacket.data, 0, requestPacket.length)
        val num = buffer.getLong(0)
        val time = buffer.getLong(Long.SIZE_BYTES)
        val message = Message(num, time)

        storage.handleMessage(requestPacket.socketAddress as InetSocketAddress, message)
    }
}