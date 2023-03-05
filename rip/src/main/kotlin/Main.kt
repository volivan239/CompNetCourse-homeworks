import kotlinx.serialization.json.*
import kotlinx.serialization.*

private val format = Json { isLenient = true }

fun main() {
    val jsonConfig = ::main::class.java.classLoader.getResource("netConfig.json")?.readText()
        ?: error("Net config found")
    val config = format.decodeFromString<NetConfig>(jsonConfig)
    val net = Net(config)
    Thread.sleep(500L)
    net.finishAll()
    config.router2Connections.keys.forEach { ip ->
        println("Final config of router $ip:")
        net.getRouter(ip).prettyPrintState()
    }
}