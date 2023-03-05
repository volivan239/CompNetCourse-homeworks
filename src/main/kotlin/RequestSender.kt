import rawhttp.core.RawHttp
import rawhttp.core.RawHttpHeaders
import rawhttp.core.RawHttpRequest
import rawhttp.core.RawHttpResponse
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Socket

object RequestSender {

    private const val defaultPort: Int = 80

    private fun sendRequest(request: RawHttpRequest): RawHttpResponse<*> {
        val host = request.uri.host
        val port = if (request.uri.port != -1) {
            request.uri.port
        } else {
            defaultPort
        }

        val clientSocket = try {
            Socket(host, port)
        } catch (_: IOException) {
            return Responder.notFoundResponse
        }

        return clientSocket.use {
            request.writeTo(clientSocket.getOutputStream())
            RawHttp().parseResponse(clientSocket.getInputStream()).eagerly()
        }.also {
            Journal.addRecord(request, it)
        }
    }

    fun sendRequestWithCacheCheck(request: RawHttpRequest): RawHttpResponse<*> {
        val cachedResponse = Journal.getCachedResponse(request)

        if (request.method != "GET" || cachedResponse == null) {
            return sendRequest(request)
        }

        val lastModified = cachedResponse.headers["Last-Modified"].firstOrNull()
        val etag = cachedResponse.headers["ETag"].firstOrNull()

        val headersBuilder = RawHttpHeaders.newBuilder()
        if (lastModified != null) {
            headersBuilder.with("If-Modified-Since", lastModified)
        }
        if (etag != null) {
            headersBuilder.with("If-None-Match", etag)
        }

        val newRequest = request.withHeaders(request.headers.and(headersBuilder.build()))
        val response = sendRequest(newRequest)

        if (response.statusCode != HttpURLConnection.HTTP_NOT_MODIFIED) {
            return response
        }
        return cachedResponse
    }
}