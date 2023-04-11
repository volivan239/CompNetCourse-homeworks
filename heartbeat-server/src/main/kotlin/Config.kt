import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource

data class Config(
    val port: Int,
    val timelimit: Long
)

val config = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.json")
    .build()
    .loadConfigOrThrow<Config>()