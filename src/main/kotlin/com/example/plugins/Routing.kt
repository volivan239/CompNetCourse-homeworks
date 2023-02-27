package com.example.plugins

import com.example.routes.productRouting
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        productRouting()
    }
}
