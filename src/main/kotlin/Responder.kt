import rawhttp.core.*
import rawhttp.core.body.BytesBody
import java.net.HttpURLConnection

object Responder {
    private val version = HttpVersion.HTTP_1_1

    private val blockedPageStatusLine = StatusLine(version, HttpURLConnection.HTTP_BAD_REQUEST, "Bad request")
    private val invalidURLStatusLine = StatusLine(version, HttpURLConnection.HTTP_BAD_REQUEST, "Can't parse given URL")
    private val notFoundStatusLine = StatusLine(version, HttpURLConnection.HTTP_NOT_FOUND, "Not found")
    private val methodNotAllowedStatusLine = StatusLine(version, HttpURLConnection.HTTP_BAD_METHOD, "Method not allowed")

    private fun getErrorResponse(statusLine: StatusLine): RawHttpResponse<*> =
        RawHttpResponse(null, null, statusLine, RawHttpHeaders.empty(), null)

    val blockedPageResponse: RawHttpResponse<*>
        get() {
            val headers = RawHttpHeaders.newBuilder().with("Content-type", "text/plain").build()
            val body = BytesBody("The requested URL is blocked by the proxy".toByteArray())
            return RawHttpResponse(null, null, blockedPageStatusLine, headers, body.toBodyReader().eager())
        }

    val notFoundResponse: RawHttpResponse<*>
        get() = getErrorResponse(notFoundStatusLine)

    val methodNotAllowedResponse: RawHttpResponse<*>
        get() = getErrorResponse(methodNotAllowedStatusLine)

    val invalidURLResponse: RawHttpResponse<*>
        get() = getErrorResponse(invalidURLStatusLine)
}