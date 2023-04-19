package aliexpresstcp

import kotlinx.serialization.Serializable

@Serializable
class AliexpressTCPPacket(
    val num: Int,
    val ack: Boolean,
    val fin: Boolean,
    val data: ByteArray
) {
    fun isAckFor(originalPacket: AliexpressTCPPacket): Boolean {
        return num == originalPacket.num && ack && fin == originalPacket.fin && data.isEmpty()
    }

    override fun toString(): String = buildString {
        if (fin) {
            append("[FIN] ")
        }
        if (ack) {
            append("[ACK] ")
        }
        append("num = $num, data size = ${data.size}")
    }

    companion object {
        fun getAckFor(originalPacket: AliexpressTCPPacket): AliexpressTCPPacket {
            return AliexpressTCPPacket(originalPacket.num, ack = true, originalPacket.fin, byteArrayOf())
        }
    }
}