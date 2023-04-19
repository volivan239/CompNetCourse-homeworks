import aliexpresstcp.AliexpressTcpSocket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf
import java.net.InetSocketAddress

@Serializable
enum class QueryType {
    GET,
    UPLOAD
}

@Serializable
class Query(val type: QueryType, val fileName: String, val contents: ByteArray)

@Serializable
class Response(val success: Boolean, val data: ByteArray)

@OptIn(ExperimentalSerializationApi::class)
fun AliexpressTcpSocket.sendQuery(query: Query, serverAddress: InetSocketAddress): Response? {
    if (!sendData(ProtoBuf.encodeToByteArray(query), serverAddress)) {
        return null
    }

    val (rawResponse, _) = receiveAll() ?: return null

    return ProtoBuf.decodeFromByteArray<Response>(rawResponse)
}

@OptIn(ExperimentalSerializationApi::class)
fun AliexpressTcpSocket.receiveQuery(): Pair<Query, InetSocketAddress>? {
    val (rawQuery, clientAddress) = receiveAll() ?: return null

    return ProtoBuf.decodeFromByteArray<Query>(rawQuery) to clientAddress
}

@OptIn(ExperimentalSerializationApi::class)
fun AliexpressTcpSocket.sendResponse(response: Response, clientAddress: InetSocketAddress): Boolean {
    return sendData(ProtoBuf.encodeToByteArray(response), clientAddress)
}