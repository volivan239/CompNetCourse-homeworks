sealed class Update(val neighbour: Node)

class EdgeUpdate(neighbour: Node, val distance: Int): Update(neighbour)

class DVUpdate(neighbour: Node, val dv: Map<Node, Int>): Update(neighbour)