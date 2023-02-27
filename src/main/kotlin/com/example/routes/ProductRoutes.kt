package com.example.routes

import com.example.models.Product
import com.example.models.ProductBody
import com.example.models.productStorage
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.productRouting() {
    route("/") {
        get {
            if (productStorage.isEmpty()) {
                productStorage.add(Product.createByBody(ProductBody("Test product", "This is a sample of product")))
            }
            call.respond(productStorage)
        }
        get("{id?}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Malformed id")
            val product = productStorage.firstOrNull { it.id == id } ?: return@get call.respond(HttpStatusCode.NotFound, "id not found")
            call.respond(product)
        }
        post {
            val body = call.receive<ProductBody>()
            Product.createByBody(body).let {
                productStorage.add(it)
                call.respond(HttpStatusCode.Created, it.id)
            }
        }
        put("{id?}") {
            val id = call.parameters["id"]?: return@put call.respond(HttpStatusCode.BadRequest, "Malformed id")
            val idx = productStorage.indexOfFirst { it.id == id }
            if (idx == -1) {
                call.respond(HttpStatusCode.NotFound, "id not found")
            }
            val newBody = call.receive<ProductBody>()
            productStorage[idx] = Product(id, newBody)
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