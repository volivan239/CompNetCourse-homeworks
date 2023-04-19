package aliexpresstcp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@Serializable
@OptIn(ExperimentalSerializationApi::class)
class AliexpressTCPPacket private constructor (
    val num: Int,
    val ack: Boolean,
    val fin: Boolean,
    val checkSum: Int,
    val data: ByteArray
) {

    constructor(num: Int, ack: Boolean, fin: Boolean, data: ByteArray) : this(
        num = num,
        ack = ack,
        fin = fin,
        checkSum = AliexpressTCPPacket(num, ack, fin, 0, data).generateCheckSum(),
        data = data
    )

    private fun generateCheckSum(): Int {
        require(checkSum == 0)
        return ProtoBuf.encodeToByteArray(this).checkSum
    }

    fun validateCheckSum(): Boolean {
        val zeroCheckSumCopy = AliexpressTCPPacket(num, ack, fin, checkSum = 0, data)
        return ProtoBuf.encodeToByteArray(zeroCheckSumCopy).validateCheckSum(checkSum)
    }

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