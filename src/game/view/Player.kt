package game.view

import org.joml.Matrix4f

class Player(startPos: Int) {
	var positions : ArrayList<Int> = arrayListOf(startPos)
	var hasClone : Boolean = false
	var obtainedBox : Box? = null

	fun getPos(timeStep: Int) : Int {
		if (timeStep >= positions.size) {
			return positions.last()
		} else if (timeStep < 0) {
			return positions.first()
		}
		return positions[timeStep]
	}

	fun getPrevious(timeStep : Int) : Int {
		return getPos(timeStep - 1)
	}

	fun move(pos : Int) {
		positions.add(pos)
	}

	fun getMat(base : Matrix4f, level: Level, time : Int, interp : Float) : Matrix4f {
		val t = 3 * interp * interp - 2 * interp * interp * interp
		val from = level.nodes[getPos(time)]
		val to = level.nodes[getPos(time + 1)]
		return base.translate(from.x + t * (to.x - from.x), from.y + t * (to.y - from.y), 0f, Matrix4f()).scale(0.015f)
	}
}