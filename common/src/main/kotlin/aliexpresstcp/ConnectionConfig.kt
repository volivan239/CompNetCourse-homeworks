package aliexpresstcp

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class ConnectionConfig(
    val timeoutMillis: Int,
    val dropRate: Float,
    val maxDataSize: Int,
    val windowSize: Int,
)

val defaultConfig = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.yaml")
    .build()
    .loadConfigOrThrow<ConnectionConfig>()