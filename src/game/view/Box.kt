package game.view

class Box(startPos: Int) {
	var positions : ArrayList<Int> = arrayListOf()
	var startTime = -1
	var startEdge = startPos

	private var startPosition = startPos

	fun getPos(timeStep: Int) : Int {
		if (timeStep - startTime >= positions.size) {
			return positions.last()
		} else if (timeStep - startTime < 0) {
			return positions.first()
		}
		return positions[timeStep - startTime]
	}

	fun newTimeCycle() {
		if (startTime > 0) {
			startTime = 0
			val pos = positions.last()
			positions.clear()
			positions.add(pos)
		}
	}

	fun reset() {
		startEdge = startPosition
		positions.clear()
		startTime = -1
	}
}