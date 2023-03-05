import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class Config(
    val address: String,
    val port: Int
)

val config = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.json")
    .build()
    .loadConfigOrThrow<Config>()