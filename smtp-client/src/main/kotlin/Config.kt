import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

enum class Client { Raw, LibraryBased }

data class Config(
    val client: Client,
    val host: String,
    val port: Int,
    val auth: Boolean,
    val tls: Boolean,
    val username: String,
    val password: String,
    val debug: Boolean
)

val config = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.json")
    .build()
    .loadConfigOrThrow<Config>()