@file:OptIn(ExperimentalSerializationApi::class)

import aliexpresstcp.GoBackNSocket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.InetSocketAddress

@Serializable
class Query(val fileName: String, val contents: ByteArray)

fun GoBackNSocket.sendQuery(query: Query, serverAddress: InetSocketAddress) {
    sendData(ProtoBuf.encodeToByteArray(query), serverAddress)
}

fun GoBackNSocket.receiveQuery(): Pair<Query, InetSocketAddress> {
    val (rawQuery, clientAddress) = receiveAll()

    return ProtoBuf.decodeFromByteArray<Query>(rawQuery) to clientAddress
}