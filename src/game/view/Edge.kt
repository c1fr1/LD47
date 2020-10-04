package game.view

class Edge(av : Int, bv : Int) {
	val a : Int = av
	val b : Int = bv
	var hasClone = false
	private var shouldHaveClone = false
	fun getOther(i : Int) : Int {
		if (a == i) {
			return b
		}
		if (b == i) {
			return a
		}
		return -1
	}
	fun setCloneEdge() {
		shouldHaveClone = true
		reset()
	}
	fun reset() {
		hasClone = shouldHaveClone
	}
}