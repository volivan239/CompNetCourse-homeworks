package com.example.models

import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.concurrent.TimeUnit

class WrongProductInfoException: IllegalArgumentException()

@Serializable
data class ProductUploadInfo(val name: String, val desc: String, val imgURL: String?)

@Serializable
data class ProductInfo(val id: String, val name: String, val desc: String)

class Product(val id: String, val name: String, val desc: String, val img: ByteArray?) {
    fun toProductInfo() = ProductInfo(id, name, desc)

    companion object {
        fun createByUploadInfo(info: ProductUploadInfo, id: String? = null): Product {
            val imgURL = info.imgURL ?:
                return Product(id ?: UUID.randomUUID().toString(), info.name, info.desc, null)

            val client = OkHttpClient().newBuilder()
                .connectTimeout(3L, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder().url(imgURL).build()
            val response = client.newCall(request).execute()
            val body = response.body

            if (response.isSuccessful && body != null && body.contentType()?.subtype == "png") {
                return Product(id ?: UUID.randomUUID().toString(), info.name, info.desc, body.bytes())
            }

            throw WrongProductInfoException()
        }

        val defaultIMG: ByteArray by lazy {
            File(defaultImagePath).readBytes()
        }

        private const val defaultImagePath = "defaultImage.png"
    }
}

val productStorage = mutableListOf<Product>()