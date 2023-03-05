import com.sksamuel.hoplite.ConfigLoaderBuilder
import com.sksamuel.hoplite.addResourceSource
import java.io.File

data class Config(val port: Int, val banned: List<String>, val cache: String) {
    init {
        val cacheDir = File(cache)
        if (!cacheDir.exists()) {
            cacheDir.mkdir()
        }
        if (!cacheDir.isDirectory) {
            println("Can't find or create cache directory")
        }
    }
}

val config = ConfigLoaderBuilder
    .default()
    .addResourceSource("/config.json")
    .build()
    .loadConfigOrThrow<Config>()