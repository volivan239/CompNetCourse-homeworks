import kotlin.IllegalArgumentException

typealias IP = String

class Net(config: NetConfig) {
    private val ip2Router = config.router2Connections.entries.associate { (ip, connections) ->
        ip to Router(ip, this, connections.toSet()).also { it.start() }
    }

    fun getRouter(ip: IP): Router {
        return ip2Router[ip] ?: throw IllegalArgumentException("Router not found")
    }

    val allTables: Map<Router, DestTable>
        get() = ip2Router.values.associateWith { it.destTable }

    fun finishAll() {
        ip2Router.values.forEach {
            it.interrupt()
            it.join()
        }
    }
}