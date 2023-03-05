import java.util.concurrent.LinkedBlockingQueue

data class DestTableValue(val distance: Int, val nextRouterIP: IP)
typealias DestTable = Map<IP, DestTableValue>

class Router(val ip: IP, private val net: Net, private val connections: Set<IP>): Thread() {
    private val connectionDestTables: MutableMap<IP, DestTable> = connections.associateWith { mapOf(it to DestTableValue(0, it)) }.toMutableMap()
    private val updates = LinkedBlockingQueue<Pair<IP, DestTable>>()

    @Volatile
    var destTable: DestTable = mapOf(ip to DestTableValue(0, ip))
        private set

    private fun addUpdate(updatedRouterIP: IP, newDestTable: DestTable) {
        updates.put(updatedRouterIP to newDestTable)
    }

    private fun recalcDests(forceSendUpdate: Boolean = false) {
        val newDestTable: MutableMap<IP, DestTableValue> = mutableMapOf(ip to DestTableValue(0, ip))
        connectionDestTables.forEach { (connection, destTable) ->
            destTable.forEach { (dest, destTableValue) ->
                val relaxValue = DestTableValue(destTableValue.distance + 1, connection)
                val cur = newDestTable.getOrPut(dest) { relaxValue }
                if (relaxValue.distance < cur.distance) {
                    newDestTable[dest] = relaxValue
                }
            }
        }

        if (forceSendUpdate || destTable != newDestTable) {
            destTable = newDestTable
            connections.forEach { ip ->
                net.getRouter(ip).addUpdate(this.ip, newDestTable)
            }
        }
    }

    fun prettyPrintState() {
        println("[Source IP]     [Destination IP]        [Next hop]        [Distance]")
        destTable.forEach { (ip, destTableValue) ->
            println("${this.ip}     $ip             ${destTableValue.nextRouterIP}       ${destTableValue.distance}")
        }
    }

    override fun run() {
        var counter = 0

        synchronized(net) {
            println("Simulation step 0 of router $ip:")
            prettyPrintState()
        }
        recalcDests(forceSendUpdate = true)

        while (!isInterrupted) {
            val (connection, newConnectionDestTable) = try {
                updates.take()
            } catch (_: InterruptedException) {
                break
            }

            connectionDestTables[connection] = newConnectionDestTable
            recalcDests()

            synchronized(net) {
                println("Simulation step ${++counter} of router $ip:")
                prettyPrintState()
            }
        }
    }
}