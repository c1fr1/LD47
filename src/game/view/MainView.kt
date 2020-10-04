package game.view

import engine.EnigView
import engine.OpenGL.*
import org.joml.Matrix4f
import org.joml.Vector2f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11
import kotlin.math.PI
import kotlin.math.cos

fun main(args : Array<String>) {
	EnigWindow.runOpeningSequence = false
	val window = EnigWindow("LD47")
	window.fps = 60
	GL11.glDisable(GL11.GL_CULL_FACE)
	val mainView = MainView(window)
	mainView.runLoop()
	window.terminate()
}

class MainView : EnigView {

	val rect : VAO
	val circle : VAO
	val colorShader : ShaderProgram
	val baseMat : Matrix4f
	val textRenderer : TextRenderer

	var player = Player(0)
	var clones = ArrayList<Player>(0)
	var numClonesQueued = 0
	var time = 0

	var interpTimer = 0.0f
	var animTimer = 0.0f
	var isMoving = false

	var topText : String? = null
	var topTextTimer = 0f

	var currentLevel = 0

	var currentCycle = 0

	var shouldNextLevel = false

	val levels = arrayOf(Level(arrayOf(
			Vector2f(-1f, 0f),
			Vector2f(-0.5f, 0.35f),
			Vector2f(0f, 0.7f),
			Vector2f(-0.5f, -0.35f),
			Vector2f(0f, 0f),
			Vector2f(0.5f, 0.35f),
			Vector2f(0f, -0.7f),
			Vector2f(0.5f, -0.35f),
			Vector2f(1f, 0f)
	), arrayOf(
			Edge(0, 1),
			Edge(1, 2),
			Edge(0, 3),
			Edge(1, 4),
			Edge(2, 5),
			Edge(3, 4),
			Edge(4, 5),
			Edge(3, 6),
			Edge(4, 7),
			Edge(5, 8),
			Edge(6, 7),
			Edge(7, 8)
	), 0, 8, 1), Level(arrayOf(
			Vector2f(-1f, 0f),
			Vector2f(-0.5f, 0f),
			Vector2f(0.5f, 0f),
			Vector2f(1f, 0f),
			Vector2f(0f, 0.6f),
			Vector2f(-0.5f, -0.6f)
	), arrayOf(
			Edge(0, 1),
			Edge(1, 2),
			Edge(2, 3),
			Edge(1, 4),
			Edge(2, 4),
			Edge(2, 5),
			Edge(0, 5)

	), 4, 3, 1).addBoxes(arrayOf(1)).addWBs(arrayOf(WallButtonPair(0, 2))), /*Level(arrayOf(
			Vector2f(1f, 0f),
			Vector2f(0f, 0f),
			Vector2f(-0.5f, 0.55f),
			Vector2f(-1f, 0f),
			Vector2f(-0.5f, -0.55f)
	), arrayOf(
			Edge(0, 1),
			Edge(1, 2),
			Edge(2, 3),
			Edge(3, 4),
			Edge(1, 4),
			Edge(2, 4)

	), 1, 0, 2).addBoxes(arrayOf(1)).addWBs(arrayOf(WallButtonPair(3, 0))).addClones(arrayOf(2)), */Level(arrayOf(
			Vector2f(0f, 0f),
			Vector2f(0f, 0.2f),
			Vector2f(-0.1f, 0.1f),
			Vector2f(0.1f, 0.1f),
			Vector2f(0f, 0.4f),
			Vector2f(0f, 0.6f),
			Vector2f(0f, 0.8f),
			Vector2f(-0.25f, 0f),
			Vector2f(-0.5f, 0f),
			Vector2f(0f, -0.25f),
			Vector2f(0f, -0.5f),
			Vector2f(0.25f, 0f),
			Vector2f(0.5f, 0f)

	), arrayOf(
			Edge(2, 3),
			Edge(0, 2),
			Edge(0, 3),
			Edge(2, 1),
			Edge(3, 1),
			Edge(1, 4),
			Edge(4, 5),
			Edge(5, 6),
			Edge(8, 9),
			Edge(7, 8),
			Edge(0, 9),
			Edge(9, 10),
			Edge(0, 11),
			Edge(11, 12),
			Edge(8, 10),
			Edge(10, 12),
			Edge(7, 2),
			Edge(9, 11),
			Edge(0, 7)
	), 0, 6, 2).addClones(arrayOf(8, 10, 12)).addBoxes(arrayOf(13, 11, 3)).addWBs(arrayOf(WallButtonPair(8, 5), WallButtonPair(10, 6), WallButtonPair(12, 7))),
			Level(arrayOf(
					Vector2f(0f, 0f),
					Vector2f(0f, 0f)
			), arrayOf(), 0, 1, 1)
	)

	var lvl = levels[0]/*Level(arrayOf(
			Vector2f(-1f, -0.5f),
			Vector2f(-1f, 0.5f),
			Vector2f(0.0f, 0.5f),
			Vector2f(0.0f, -0.5f),
			Vector2f(1f, -0.5f),
			Vector2f(1f, 0f),
			Vector2f(1f, 0.5f)
	), arrayOf(
			Edge(0, 1),
			Edge(0, 2),
			Edge(0, 3),
			Edge(1, 2),
			Edge(2, 3),
			Edge(3, 4),
			Edge(3, 5),
			Edge(4, 5),
			Edge(5, 6)
	), 3, 6)*/

	constructor(window : EnigWindow) : super(window) {
		rect = VAO(0f, -0.5f, 1f, 1f)
		colorShader = ShaderProgram("colorShader")
		baseMat = window.getSquarePerspectiveMatrix(2f)
		circle = VAO("res/objects/circle.obj")
		textRenderer = TextRenderer()
		/*lvl.edges[1].hasClone = true
		lvl.edges[4].hasClone = true
		lvl.edges[5].hasClone = true
		val box = Box(3)
		lvl.boxes.add(box)
		lvl.wbuttons.add(WallButtonPair(1, 8))*/
	}

	override fun loop() : Boolean {
		animTimer += deltaTime
		animTimer %= 1f
		topTextTimer -= deltaTime
		FBO.prepareDefaultRender()
		colorShader.enable()
		renderPlayerAndClones()
		val edge = getMoveEdge()
		var idxNotAllowedError = isValidMove(edge)
		tryMove(edge, idxNotAllowedError)
		lvl.draw(rect, circle, baseMat, edge, idxNotAllowedError == null, animTimer, interpTimer, time)

		if (topText != null && topTextTimer > 0f) {
			textRenderer.drawText(baseMat.translate(-1f, 0.9f, 0f, Matrix4f()).scale(0.1f), topText!!)
		}

		if (currentLevel == 0) {
			textRenderer.drawText(baseMat.translate(-1f, -0.95f, 0f, Matrix4f()).scale(0.1f), "Click on edges to move to the exit.")
		}
		if (currentLevel == 1) {
			textRenderer.drawText(baseMat.translate(-1f, -0.95f, 0f, Matrix4f()).scale(0.1f), "Push boxes onto buttons to open doors.")
			textRenderer.drawText(baseMat.translate(-1f, -0.85f, 0f, Matrix4f()).scale(0.1f), "Returning to the start node starts a new cycle.")
		}
		/*if (currentLevel == 2) {
			textRenderer.drawText(baseMat.translate(-1f, -0.95f, 0f, Matrix4f()).scale(0.1f), "Returning to the start with a clone restarts the cycle.")
		}*/
		if (currentLevel == 3) {
			textRenderer.drawText(baseMat.translate(-1f, -0.95f, 0f, Matrix4f()).scale(0.1f), "You Win!")
		} else {
			textRenderer.drawText(baseMat.translate(-1f, 0.8f, 0f, Matrix4f()).scale(0.1f), "Cycle Limit: ${lvl.roundRequirement}")
			textRenderer.drawText(baseMat.translate(-1f, 0.7f, 0f, Matrix4f()).scale(0.1f), "Current Cycle: ${currentCycle}")
		}


		if (currentCycle == lvl.roundRequirement && !shouldNextLevel && !isMoving) {
			reset()
			topTextTimer = 5f
			topText = "Cycle Limit Reached!"
		}

		if (shouldNextLevel) {
			nextLevel()
			shouldNextLevel = false
		}

		if (window.keys[GLFW_KEY_R] == 1) {
			reset()
		}

		if (window.keys[GLFW_KEY_ESCAPE] != 0) {
			return true
		}
		return false
	}

	fun renderPlayerAndClones() {
		if (isMoving) {
			interpTimer += 2 * deltaTime
			if (interpTimer > 1f) {
				time += 1
				interpTimer = 0f
				isMoving = false
				if (player.getPos(time) == lvl.startNode) {
					time = 0
					player = Player(lvl.startNode)
				} else if (player.getPos(time) == lvl.endNode) {
					time = 0
					if (numClonesQueued + countNonEscapingClones() == 0) {
						shouldNextLevel = true
					} else {
						player = Player(lvl.startNode)
					}
				}
			}
		} else {
			interpTimer = (interpTimer + deltaTime / 3) % 0.3f
		}
		val playerMat = player.getMat(baseMat, lvl, time, interpTimer)
		circle.prepareRender()
		if (player.hasClone) {
			val animSize = (cos(2f * PI * animTimer).toFloat() + 1f) / 5f
			colorShader.setUniform(2, 0, 0.5f, 0.5f, 1.0f)
			colorShader.setUniform(0, 0, playerMat.scale((1f + animSize) / 1.5f, Matrix4f()))
			circle.drawTriangles()
		}
		colorShader.setUniform(2, 0, 0.0f, 0.0f, 1.0f)
		colorShader.setUniform(0, 0, playerMat)
		circle.drawTriangles()
		colorShader.setUniform(2, 0, 0.5f, 0.5f, 1.0f)
		for (clone in clones) {
			colorShader.setUniform(0, 0, clone.getMat(baseMat, lvl, time, interpTimer))
			circle.drawTriangles()
		}
	}

	fun isValidMove(e : Int) : String? {
		if (e >= 0) {
			val otherPoint = lvl.edges[e].getOther(player.getPos(time))
			if (otherPoint == player.getPrevious(time)) {
				return "you cannot go the way you came from"
			}
			if (lvl.edges[e].hasClone && player.hasClone) {
				return "you cannot pick up multiple clones"
			}
			for (wb in lvl.wbuttons) {
				if (wb.wallEdge == e) {
					if (wb.hasBoxTime > time || wb.hasBoxTime < 0) {
						return "that door is closed " + wb.hasBoxTime
					}
				}
				if (otherPoint == wb.buttonNode && wb.hasBoxTime < time && wb.hasBoxTime >= 0) {
					return "that path is blocked by a box on a button"
				}
			}
			for (box in lvl.boxes) {
				if (box.startEdge == e) {
					if (box.startTime > time) {
						return "moving this box would cause an interference with one of your clones later"
					}
					if (player.obtainedBox != null && box.startTime == -1) {
						return "you cannot move multiple boxes at once"
					}
				}
			}
			if (lvl.edges[e].getOther(player.getPos(time)) == lvl.endNode && player.hasClone) {
				return "you cannot exit while carrying a clone"
			}
			if (lvl.edges[e].getOther(player.getPos(time)) == lvl.endNode && player.obtainedBox != null) {
				return "you cannot exit while pushing a box"
			}
			if (lvl.edges[e].getOther(player.getPos(time)) == lvl.startNode && player.obtainedBox != null) {
				return "you cannot return to the start while pushing a box"
			}
			if (otherPoint == lvl.endNode || otherPoint == lvl.startNode) {
				return null
			}
			for (c in clones) {
				if (otherPoint == c.getPos(time + 1)) {
					return "you cannot cross paths with another clone"
				}
				if (otherPoint == c.getPos(time) && player.getPos(time) == c.getPos(time + 1)) {
					return "you cannot cross paths with another clone"
				}
			}
			return null
		}
		return "invalid edge"
	}

	fun getMoveEdge() : Int {
		var edge = lvl.getEdgeHovering(baseMat, window.cursorXFloat, window.cursorYFloat)
		if (edge >= 0) {
			if (lvl.edges[edge].a != player.getPos(time) && lvl.edges[edge].b != player.getPos(time)) {
				edge = -1
			}
		}
		return edge
	}

	fun tryMove(edge : Int, isValid : String?) {
		if (edge >= 0 && !isMoving) {
			if (window.mouseButtons[GLFW_MOUSE_BUTTON_LEFT] == 3) {
				if (isValid == null) {
					move(lvl.edges[edge].getOther(player.getPos(time)), edge)
				} else {
					topText = isValid
					topTextTimer = 5f
				}
			}
		}
	}

	fun move(nextPos : Int, ei : Int) {
		val e : Edge = lvl.edges[ei]
		player.move(nextPos)
		player.obtainedBox?.positions?.add(nextPos)
		isMoving = true
		if (e.hasClone) {
			e.hasClone = false
			player.hasClone = true
		}
		val npos = player.getPos(time + 1)
		for (box in lvl.boxes) {
			if (box.startEdge == ei && box.startTime == -1) {
				box.startTime = time + 1
				box.positions.add(nextPos)
				player.obtainedBox = box
			}
		}
		for (wb in lvl.wbuttons) {
			if (wb.buttonNode == nextPos && player.obtainedBox != null) {
				wb.hasBoxTime = time
				player.obtainedBox = null
			}
		}
		if (npos == lvl.startNode || npos == lvl.endNode) {
			if (player.hasClone) {
				numClonesQueued++
				player.hasClone = false
			}
			if (numClonesQueued > 0) {
				clones.add(player)
				--numClonesQueued
			} else {
				clones.add(player)
				startNewCycle()
			}
		}
	}

	fun startNewCycle() {
		numClonesQueued = countNonEscapingClones()
		++currentCycle
		System.out.println(numClonesQueued)
		clones.clear()
		for (wb in lvl.wbuttons) {
			wb.newTimeCycle()
		}
		for (b in lvl.boxes) {
			b.newTimeCycle()
		}
	}

	fun countNonEscapingClones() : Int {
		var ret = 0
		for (c in clones) {
			if (c.positions.last() == lvl.startNode) {
				++ret
			}
		}
		return ret
	}

	fun nextLevel() {
		System.out.println("next leveling")
		if (currentLevel != levels.size - 1) {
			setLevel(levels[++currentLevel])
		}
	}

	fun setLevel(l : Level) {
		lvl = l
		player.positions.clear()
		clones.clear()
		numClonesQueued = 0
		player.positions.add(l.startNode)
		time = 0
		currentCycle = 0
	}

	fun reset() {
		lvl.reset()
		player.positions.clear()
		clones.clear()
		numClonesQueued = 0
		player.positions.add(lvl.startNode)
		currentCycle = 0
		time = 0
		player.obtainedBox = null
		player.hasClone = false
	}
}
