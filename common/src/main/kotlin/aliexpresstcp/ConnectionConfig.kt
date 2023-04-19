package aliexpresstcp

import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class ConnectionConfig(
    val ackTimeoutMillis: Int,
    val dropRate: Float,
    val corruptionRate: Float,
    val maxDataSize: Int,
    val senderMaxFinCount: Int,
    val receiverTimeoutMillis: Int,
)

val defaultConfig = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.yaml")
    .build()
    .loadConfigOrThrow<ConnectionConfig>()