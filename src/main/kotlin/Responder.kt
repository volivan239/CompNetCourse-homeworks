import rawhttp.core.*
import rawhttp.core.body.FileBody
import java.io.File
import java.nio.file.Files

object Responder {
    private val version = HttpVersion.HTTP_1_1

    private val okStatusLine         = StatusLine(version, 200, "OK")
    private val notFoundStatusLine   = StatusLine(version, 404, "Not found")
    private val badRequestStatusLine = StatusLine(version, 405, "Method not allowed")

    private val File.contentType
        get() = Files.probeContentType(toPath())

    val notFoundResponse: RawHttpResponse<*>
        get() = RawHttpResponse(null, null, notFoundStatusLine, RawHttpHeaders.empty(), null)

    val badRequestResponse: RawHttpResponse<*>
        get() = RawHttpResponse(null, null, badRequestStatusLine, RawHttpHeaders.empty(), null)

    fun getOkResponse(file: File): RawHttpResponse<*> {
        val headers = RawHttpHeaders.newBuilder().with("Content-type", file.contentType).build()
        return RawHttpResponse(null, null, okStatusLine, headers, FileBody(file).toBodyReader())
    }
}