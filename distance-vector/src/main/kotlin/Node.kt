import java.util.concurrent.LinkedBlockingQueue

class Node(val id: Int): Thread() {
    private data class NeighbourInfo(val distance: Int, val dv: Map<Node, Int>)

    private val neighbours: MutableMap<Node, NeighbourInfo> = mutableMapOf()
    private val updates = LinkedBlockingQueue<Update>()

    @Volatile
    var distances: Map<Node, Int> = mapOf(this to 0)
        private set

    @Volatile
    private var isRecalcRunning = false

    fun update(upd: Update) {
        updates.put(upd)
    }

    val hasUnprocessedUpdates: Boolean
        get() = isRecalcRunning || updates.isNotEmpty()

    private fun recalcDV() {
        isRecalcRunning = true
        val newDistances = mutableMapOf(this to 0)
        neighbours.forEach { _, (c, dv) ->
            dv.forEach { (node, dist) ->
                newDistances[node] = minOf(newDistances.getOrDefault(node, c + dist), c + dist)
            }
        }

        if (newDistances != distances) {
            distances = newDistances
            neighbours.keys.forEach {
                it.update(DVUpdate(this, distances))
            }
        }
        isRecalcRunning = false
    }

    override fun run() {
        while (!isInterrupted) {
            val update = try {
                updates.take()
            } catch (_: InterruptedException) {
                break
            }

            when (update) {
                is DVUpdate -> {
                    val oldInfo = neighbours[update.neighbour]
                    require(oldInfo != null)
                    neighbours[update.neighbour] = NeighbourInfo(oldInfo.distance, update.dv)
                }
                is EdgeUpdate -> {
                    val oldInfo = neighbours.getOrDefault(
                        update.neighbour,
                        NeighbourInfo(update.distance, update.neighbour.distances)
                    )

                    if (update.distance == -1) {
                        neighbours.remove(update.neighbour)
                    } else {
                        neighbours[update.neighbour] = NeighbourInfo(update.distance, oldInfo.dv)
                    }
                }
            }
            recalcDV()
        }
    }
}