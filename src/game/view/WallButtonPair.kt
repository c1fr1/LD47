package game.view

import org.joml.Vector3f

class WallButtonPair(button : Int, wall : Int) {
	var buttonNode = button
	var wallEdge = wall
	var hasBoxTime = -1
	var color : Vector3f = Vector3f(1f, 0f, 0f)

	fun newTimeCycle() {
		if (hasBoxTime > 0) {
			hasBoxTime = 0
		}
	}

	fun reset() {
		hasBoxTime = -1
	}
}