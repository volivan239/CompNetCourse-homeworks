sealed class Update(val neighbour: Router)

class EdgeUpdate(neighbour: Router, val distance: Int): Update(neighbour)

class DVUpdate(neighbour: Router, val dv: Map<Router, Int>): Update(neighbour)