package com.example.routes

import com.example.models.Product
import com.example.models.ProductUploadInfo
import com.example.models.productStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.lang.IllegalArgumentException

fun Route.productRouting() {
    route("/") {
        get {
            if (productStorage.isEmpty()) {
                productStorage.add(Product.createByUploadInfo(ProductUploadInfo("Test product", "This is a sample of product", null)))
            }
            call.respond(productStorage.map { it.toProductInfo() })
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Malformed id")
            val product = productStorage.firstOrNull { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound, "id not found")
            call.respond(product.toProductInfo())
        }
        get("{id?}/image") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Malformed id")
            val product = productStorage.firstOrNull { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound, "id not found")
            call.respondBytes(product.img ?: Product.defaultIMG, ContentType.Image.PNG)
        }
        post {
            val info = call.receive<ProductUploadInfo>()
            val product = try {
                Product.createByUploadInfo(info)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Incorrect product info")
                return@post
            }

            productStorage.add(product)
            call.respond(HttpStatusCode.Created, product.id)
        }
        put("{id?}") {
            val id = call.parameters["id"]?: return@put call.respond(HttpStatusCode.BadRequest, "Malformed id")
            val idx = productStorage.indexOfFirst { it.id == id }
            if (idx == -1) {
                call.respond(HttpStatusCode.NotFound, "id not found")
            }
            val newInfo = call.receive<ProductUploadInfo>()
            try {
                productStorage[idx] = Product.createByUploadInfo(newInfo, id)
            } catch (_: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, "Incorrect product info")
                return@put
            }
            call.respond(HttpStatusCode.OK, "Successfully updated")
        }
        delete("{id?}") {
            val id = call.parameters["id"]?: return@delete call.respond(HttpStatusCode.BadRequest, "Malformed id")
            if (productStorage.removeIf { it.id == id })
                call.respond(HttpStatusCode.OK, "Successfully deleted")
            else
                call.respond(HttpStatusCode.NotFound, "id not found")
        }
    }
}