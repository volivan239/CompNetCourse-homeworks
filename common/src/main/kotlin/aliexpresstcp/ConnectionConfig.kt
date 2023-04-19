package aliexpresstcp

data class ConnectionConfig(
    val ackTimeoutMillis: Int,
    val dropRate: Float,
    val maxDataSize: Int,
    val senderMaxFinCount: Int,
    val receiverTimeoutMillis: Int,
    val enableLogging: Boolean,
)

val defaultConfig = ConnectionConfig(
    ackTimeoutMillis = 1000,
    dropRate = 0.3f,
    maxDataSize = 25 * 1024,
    senderMaxFinCount = 5,
    receiverTimeoutMillis = 5000,
    enableLogging = true,
)
