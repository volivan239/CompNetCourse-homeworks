import kotlin.IllegalArgumentException

class Net(nodes: Set<Int>, initConnections: Map<Pair<Int, Int>, Int>) {
    private val id2Node = nodes.associateWith { id -> Node(id).also { it.start() } }

    private fun getNode(id: Int): Node {
        return id2Node[id] ?: throw IllegalArgumentException("Node not found")
    }

    init {
        initConnections.forEach { (v, u), w ->
            getNode(v).update(EdgeUpdate(getNode(u), w))
            getNode(u).update(EdgeUpdate(getNode(v), w))
        }
    }

    fun updateEdge(v: Int, u: Int, w: Int) {
        getNode(v).update(EdgeUpdate(getNode(u), w))
        getNode(u).update(EdgeUpdate(getNode(v), w))
    }

    val allDistances: Map<Int, Map<Int, Int>>
        get() = id2Node.values.associate { node ->
            node.id to node.distances.mapKeys { (other, _) -> other.id }
        }

    val anyHasUnprocessedUpdates: Boolean
        get() = id2Node.values.any { it.hasUnprocessedUpdates }

    fun finishAll() {
        id2Node.values.forEach {
            it.interrupt()
            it.join()
        }
    }
}