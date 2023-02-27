package com.example.models

import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class ProductBody(val name: String, val desc: String)

@Serializable
data class Product(val id: String, val body: ProductBody) {
    companion object {
        fun createByBody(body: ProductBody) = Product(UUID.randomUUID().toString(), body)
    }
}

val productStorage = mutableListOf<Product>()

