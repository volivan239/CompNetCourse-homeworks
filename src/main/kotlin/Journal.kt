import rawhttp.core.RawHttp
import rawhttp.core.RawHttpRequest
import rawhttp.core.RawHttpResponse
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.util.*

data class RequestRecord(val method: String, val requestURL: URI, val response: Int, val uuid: UUID)

object Journal {
    private val records: MutableList<RequestRecord> = mutableListOf()

    fun getCachedResponse(request: RawHttpRequest): RawHttpResponse<*>? = synchronized(this) {
        val uuid = request.uuid
        val file = File("${config.cache}/$uuid")
        if (!file.exists()) {
            return@synchronized null
        }
        try {
            RawHttp().parseResponse(file)
        } catch (_: Exception) {
            null
        }
    }

    fun addRecord(request: RawHttpRequest, response: RawHttpResponse<*>) = synchronized(this) {
        val uuid = request.uuid
        if (request.method == "GET" && response.statusCode == HttpURLConnection.HTTP_OK) {
            val file = "${config.cache}/$uuid"
            response.writeTo(File(file).outputStream())
        }
        records.add(RequestRecord(request.method, request.uri, response.statusCode, uuid))
    }

    private val RawHttpRequest.uuid: UUID
        get() = UUID.nameUUIDFromBytes(uri.toString().toByteArray())
}