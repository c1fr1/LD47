package game.view

import engine.OpenGL.ShaderProgram
import engine.OpenGL.VAO
import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector4f
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos

class Level(ns : Array<Vector2f>, es : Array<Edge>, sn : Int, en : Int, rr: Int) {
	var nodes : Array<Vector2f> = ns
	var edges : Array<Edge> = es
	var boxes : ArrayList<Box> = arrayListOf()
	var startNode = sn
	var endNode = en
	val roundRequirement = rr

	var wbuttons : ArrayList<WallButtonPair> = arrayListOf()

	fun draw(rvao : VAO, cvao : VAO, mat : Matrix4f, indexSel : Int, indexAllowed : Boolean, timer : Float, animTimer : Float, time : Int) {
		cvao.prepareRender()
		nodes.forEachIndexed { i, n ->
			if (i == startNode) {
				ShaderProgram.currentShaderProgram.setUniform(2, 0, 0f, 0f, 1f)
			} else if (i == endNode) {
				ShaderProgram.currentShaderProgram.setUniform(2, 0, 0f, 1f, 1f)
			} else {
				ShaderProgram.currentShaderProgram.setUniform(2, 0, 0f, 0f, 0f)
			}
			ShaderProgram.currentShaderProgram.setUniform(0, 0, mat.translate(n.x, n.y, 0f, Matrix4f()).scale(0.01f))
			cvao.drawTriangles()
			ShaderProgram.currentShaderProgram.setUniform(2, 0, 1f, 1f, 1f)
			ShaderProgram.currentShaderProgram.setUniform(0, 0, mat.translate(n.x, n.y, 0f, Matrix4f()).scale(0.013f))
			cvao.drawTriangles()
		}
		ShaderProgram.currentShaderProgram.setUniform(2, 0, 0.5f, 0.5f, 1.0f)
		val animSize = (cos(2f * PI * timer).toFloat() + 1f) / 2f
		for (e in edges) {
			if (e.hasClone) {
				val pos = nodes[e.a].add(nodes[e.b], Vector2f()).mul(0.5f)
				ShaderProgram.currentShaderProgram.setUniform(0, 0, mat.translate(pos.x, pos.y, 0f, Matrix4f()).scale(0.01f + animSize / 200.0f))
				cvao.drawTriangles()
			}
		}

		rvao.prepareRender()

		for (b in boxes) {
			val transform = matForBox(mat, b, time, animTimer)
			ShaderProgram.currentShaderProgram.setUniform(2, 0, 0.5f, 0.5f, 0.5f)
			ShaderProgram.currentShaderProgram.setUniform(0, 0, transform)
			rvao.drawTriangles()
		}

		for (wb in wbuttons) {
			ShaderProgram.currentShaderProgram.setUniform(2, 0, wb.color)
			if (wb.hasBoxTime < time && wb.hasBoxTime != -1) {
				val mats = matsForOpenWall(mat, wb, 1f)
				ShaderProgram.currentShaderProgram.setUniform(0, 0, mats[0])
				rvao.drawTriangles()
				ShaderProgram.currentShaderProgram.setUniform(0, 0, mats[1])
				rvao.drawTriangles()
			} else if (wb.hasBoxTime == time) {
				val mats = matsForOpenWall(mat, wb, animTimer)
				ShaderProgram.currentShaderProgram.setUniform(0, 0, mats[0])
				rvao.drawTriangles()
				ShaderProgram.currentShaderProgram.setUniform(0, 0, mats[1])
				rvao.drawTriangles()
			} else {
				ShaderProgram.currentShaderProgram.setUniform(0, 0, matForWall(mat, wb))
				rvao.drawTriangles()
			}
			val bmat = matsForButton(mat, wb)
			ShaderProgram.currentShaderProgram.setUniform(2, 0, 0f, 0f, 0f)
			ShaderProgram.currentShaderProgram.setUniform(0, 0, bmat[0])
			rvao.drawTriangles()
			ShaderProgram.currentShaderProgram.setUniform(2, 0, wb.color)
			ShaderProgram.currentShaderProgram.setUniform(0, 0, bmat[1])
			rvao.drawTriangles()
		}

		edges.forEachIndexed {index, e ->
			if (index == indexSel) {
				if (indexAllowed) {
					ShaderProgram.currentShaderProgram.setUniform(2, 0, 0f, 1f, 0f)
				} else {
					ShaderProgram.currentShaderProgram.setUniform(2, 0, 1f, 0f, 0f)
				}
			} else {
				ShaderProgram.currentShaderProgram.setUniform(2, 0, 1f, 1f, 1f)
			}
			ShaderProgram.currentShaderProgram.setUniform(0, 0, matForEdge(mat, e))
			rvao.drawTriangles()
		}
		rvao.unbind()
	}

	fun getEdgeHovering(mat : Matrix4f, x : Float, y : Float) : Int {
		edges.forEachIndexed { index, edge ->
			var vec = Vector4f(x, y, 0f, 1f)
			val emat = matForEdge(mat, edge).invert()
			vec.mul(emat)
			if (vec.x > 0f && vec.x < 1f && vec.y < 3f && vec.y > -3f) {
				return index
			}
		}
		return -1
	}

	fun matForEdge(mat : Matrix4f, e : Edge) : Matrix4f {
		var ret = Matrix4f()
		val a = nodes[e.a]
		val b = nodes[e.b]
		val diff = b.sub(a, Vector2f())
		mat.translate(a.x, a.y, 0f, ret)
		ret.rotateZ(atan2(diff.y, diff.x))
		ret.scale(diff.length(), 0.01f, 1f)
		return ret
	}

	fun matForWall(mat : Matrix4f, wb : WallButtonPair) : Matrix4f {
		val e = edges[wb.wallEdge]
		var ret = Matrix4f()
		val a = nodes[e.a]
		val b = nodes[e.b]
		val diff = b.sub(a, Vector2f())
		mat.translate(a.x, a.y, 0f, ret)
		ret.rotateZ(atan2(diff.y, diff.x))
		ret.translate(diff.length() / 2f, 0f, 0f)
		ret.scale(0.012f, 0.1f, 1f)
		ret.translate(-0.5f, 0f, 0f)
		return ret
	}

	fun matsForOpenWall(mat : Matrix4f, wb : WallButtonPair, animStage : Float) : Array<Matrix4f> {
		val e = edges[wb.wallEdge]
		val retA = Matrix4f()
		val retB = Matrix4f()
		val a = nodes[e.a]
		val b = nodes[e.b]
		val diff = b.sub(a, Vector2f())
		mat.translate(a.x, a.y, 0f, retA)
		retA.rotateZ(atan2(diff.y, diff.x))
		retA.translate(diff.length() / 2f, 0f, 0f)
		retA.scale(0.012f, 0.05f, 1f)
		retA.translate(-0.5f, 0.5f + animStage, 0f, retB)
		retA.translate(-0.5f, -0.5f - animStage, 0f)
		return arrayOf(retA, retB)
	}

	fun matForBox(mat : Matrix4f, box : Box, time : Int, interp : Float) : Matrix4f {
		if (time + 1 == box.startTime && interp > 0.5 && box.startTime >= 0) {
			val t = 3 * interp * interp - 2 * interp * interp * interp
			val from = nodes[edges[box.startEdge].getOther(box.getPos(time))]
			val to = nodes[box.getPos(time + 1)]
			return mat.translate(from.x  + t * (to.x - from.x), from.y + t * (to.y - from.y), 0f, Matrix4f()).scale(0.035f).translate(-0.5f, 0f, 0f)
		} else if ((time < box.startTime || box.startTime < 0)) {
			val ret = Matrix4f()
			val edge = edges[box.startEdge]
			val a = nodes[edge.a]
			val b = nodes[edge.b]
			mat.translate((a.x + b.x) / 2f, (a.y + b.y) / 2f, 0f, ret)
			ret.scale(0.035f)
			ret.translate(-0.5f, 0f, 0f)
			return ret
		} else {
			val t = 3 * interp * interp - 2 * interp * interp * interp
			val from = nodes[box.getPos(time)]
			val to = nodes[box.getPos(time + 1)]
			return mat.translate(from.x  + t * (to.x - from.x), from.y + t * (to.y - from.y), 0f, Matrix4f()).scale(0.035f).translate(-0.5f, 0f, 0f)
		}
	}

	fun matsForButton(mat : Matrix4f, wb : WallButtonPair) : Array<Matrix4f> {
		val n = nodes[wb.buttonNode]
		val ret = arrayOf(Matrix4f(), Matrix4f())
		mat.translate(n.x, n.y, 0f, ret[0]).scale(0.035f).translate(-0.5f, 0f, 0f)
		mat.translate(n.x, n.y, 0f, ret[1]).scale(0.045f).translate(-0.5f, 0f, 0f)
		return ret
	}

	fun addClones(clones : Array<Int>) : Level {
		for (clone in clones) {
			edges[clone].setCloneEdge()
		}
		return this
	}

	fun addBoxes(boxes : Array<Int>) : Level {
		for (b in boxes) {
			this.boxes.add(Box(b))
		}
		return this
	}

	fun addWBs(wbs : Array<WallButtonPair>) : Level {
		for (wb in wbs) {
			wbuttons.add(wb)
		}
		return this
	}

	fun reset() {
		for (edge in edges) {
			edge.reset()
		}
		for (b in boxes) {
			b.reset()
		}
		for (wbutton in wbuttons) {
			wbutton.reset()
		}
	}
}