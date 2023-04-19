package aliexpresstcp

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable

@Serializable
@OptIn(ExperimentalSerializationApi::class)
class GoBackNPacket(
    val num: Int,
    val fin: Boolean,
    val ack: Boolean,
    val data: ByteArray
) {
    override fun toString(): String = buildString {
        if (fin) {
            append("[FIN] ")
        }
        if (ack) {
            append("[ACK] ")
        }
        append("num = $num, data size = ${data.size}")
    }
}