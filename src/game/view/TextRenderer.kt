package game.view

import engine.OpenGL.ShaderProgram
import engine.OpenGL.Texture
import engine.OpenGL.VAO
import org.joml.Matrix4f

class TextRenderer {
	class Character(val c : Char, val x : Int, val y : Int, val width : Int, val height : Int, val originX : Int, val originY : Int, val adv : Int)

	val size = 32
	val width = 326
	val height = 120
	val characterCount = 95

	val characters = arrayOf(
		Character(' ', 20, 116, 3, 3, 1, 1, 16),
		Character('!', 211, 77, 6, 23, -2, 22, 16),
		Character('"', 238, 100, 11, 11, -1, 22, 16),
		Character('#', 236, 54, 18, 23, 1, 22, 16),
		Character('$', 135, 0, 16, 26, 0, 23, 16),
		Character('%', 30, 31, 27, 23, 0, 22, 16),
		Character('&', 109, 31, 25, 23, 0, 22, 16),
		Character('\'', 257, 100, 6, 11, 0, 22, 16),
		Character('(', 105, 0, 11, 29, 0, 22, 16),
		Character(')', 69, 0, 12, 29, 1, 22, 16),
		Character('*', 207, 100, 14, 15, -1, 23, 8),
		Character('+', 235, 77, 20, 19, 1, 20, 11),
		Character(',', 249, 100, 8, 11, 0, 5, 13),
		Character('-', 315, 100, 11, 5, 0, 9, 16),
		Character('.', 309, 100, 6, 6, -1, 5, 16),
		Character('/', 237, 0, 11, 24, 1, 23, 27),
		Character('0', 34, 77, 16, 23, 0, 22, 25),
		Character('1', 200, 77, 11, 23, -2, 22, 6),
		Character('2', 272, 54, 17, 23, 1, 22, 11),
		Character('3', 114, 77, 15, 23, 0, 22, 11),
		Character('4', 289, 54, 17, 23, 1, 22, 16),
		Character('5', 129, 77, 15, 23, 0, 22, 18),
		Character('6', 50, 77, 16, 23, 0, 22, 8),
		Character('7', 66, 77, 16, 23, 0, 22, 11),
		Character('8', 144, 77, 15, 23, -1, 22, 8),
		Character('9', 82, 77, 16, 23, 0, 22, 9),
		Character(':', 200, 100, 7, 16, -1, 15, 9),
		Character(';', 228, 77, 7, 21, -1, 15, 9),
		Character('<', 255, 77, 20, 17, 1, 19, 18),
		Character('=', 263, 100, 20, 9, 1, 15, 18),
		Character('>', 275, 77, 20, 17, 1, 19, 18),
		Character('?', 174, 77, 14, 23, 0, 22, 14),
		Character('@', 0, 0, 30, 31, 0, 23, 29),
		Character('A', 134, 31, 25, 23, 1, 22, 23),
		Character('B', 71, 54, 22, 23, 1, 22, 21),
		Character('C', 93, 54, 22, 23, 0, 22, 21),
		Character('D', 284, 31, 24, 23, 1, 22, 23),
		Character('E', 115, 54, 21, 23, 1, 22, 20),
		Character('F', 198, 54, 19, 23, 1, 22, 18),
		Character('G', 0, 54, 24, 23, 0, 22, 23),
		Character('H', 159, 31, 25, 23, 1, 22, 23),
		Character('I', 188, 77, 12, 23, 1, 22, 11),
		Character('J', 159, 77, 15, 23, 1, 22, 12),
		Character('K', 57, 31, 26, 23, 1, 22, 23),
		Character('L', 136, 54, 21, 23, 1, 22, 20),
		Character('M', 0, 31, 30, 23, 1, 22, 28),
		Character('N', 83, 31, 26, 23, 2, 22, 23),
		Character('O', 48, 54, 23, 23, 0, 22, 23),
		Character('P', 217, 54, 19, 23, 1, 22, 18),
		Character('Q', 46, 0, 23, 29, 0, 22, 23),
		Character('R', 24, 54, 24, 23, 1, 22, 21),
		Character('S', 98, 77, 16, 23, -1, 22, 18),
		Character('T', 178, 54, 20, 23, 0, 22, 20),
		Character('U', 184, 31, 25, 23, 1, 22, 23),
		Character('V', 209, 31, 25, 23, 1, 22, 23),
		Character('W', 278, 0, 32, 23, 1, 22, 30),
		Character('X', 234, 31, 25, 23, 1, 22, 23),
		Character('Y', 259, 31, 25, 23, 1, 22, 23),
		Character('Z', 157, 54, 21, 23, 1, 22, 20),
		Character('[', 116, 0, 10, 29, -1, 22, 11),
		Character('\\', 248, 0, 11, 24, 1, 23, 9),
		Character(']', 126, 0, 9, 29, 0, 22, 11),
		Character('^', 221, 100, 17, 13, 1, 22, 15),
		Character('_', 0, 116, 20, 4, 2, -4, 16),
		Character('`', 302, 100, 7, 7, -1, 22, 11),
		Character('a', 97, 100, 16, 16, 0, 15, 14),
		Character('b', 188, 0, 17, 24, 1, 23, 16),
		Character('c', 160, 100, 14, 16, 0, 15, 14),
		Character('d', 205, 0, 17, 24, 0, 23, 16),
		Character('e', 145, 100, 15, 16, 0, 15, 14),
		Character('f', 222, 0, 15, 24, 0, 23, 11),
		Character('g', 306, 54, 17, 23, 0, 15, 16),
		Character('h', 170, 0, 18, 24, 1, 23, 16),
		Character('i', 269, 0, 9, 24, 0, 23, 9),
		Character('j', 30, 0, 12, 31, 4, 23, 9),
		Character('k', 151, 0, 19, 24, 1, 23, 16),
		Character('l', 259, 0, 10, 24, 0, 23, 9),
		Character('m', 295, 77, 27, 16, 1, 15, 25),
		Character('n', 25, 100, 18, 16, 1, 15, 16),
		Character('o', 113, 100, 16, 16, 0, 15, 16),
		Character('p', 0, 77, 17, 23, 1, 15, 16),
		Character('q', 17, 77, 17, 23, 0, 15, 16),
		Character('r', 174, 100, 13, 16, 1, 15, 11),
		Character('s', 187, 100, 13, 16, 0, 15, 12),
		Character('t', 217, 77, 11, 21, 1, 20, 9),
		Character('u', 43, 100, 18, 16, 1, 15, 16),
		Character('v', 61, 100, 18, 16, 1, 15, 16),
		Character('w', 0, 100, 25, 16, 1, 15, 23),
		Character('x', 79, 100, 18, 16, 1, 15, 16),
		Character('y', 254, 54, 18, 23, 1, 15, 16),
		Character('z', 129, 100, 16, 16, 1, 15, 14),
		Character('{', 81, 0, 12, 29, -3, 22, 15),
		Character('|', 42, 0, 4, 30, -1, 23, 6),
		Character('}', 93, 0, 12, 29, -1, 22, 15),
		Character('~', 283, 100, 19, 7, 1, 12, 17)
	)

	val texture : Texture
	val vao : VAO
	val shader : ShaderProgram

	constructor() {
		texture = Texture("res/font.png")
		vao = VAO(0f, 0f, 1f, 1f)
		shader = ShaderProgram("textShader")
	}

	fun drawText(base : Matrix4f, text : String) {
		shader.enable()
		val b = Matrix4f(base)
		vao.prepareRender()
		texture.bind()
		for (char in text) {
			val co = characters[char.toInt() - 32]
			b.translate(co.adv.toFloat() / size.toFloat(), 0f, 0f)
			val tmat = Matrix4f(b)
			tmat.translate((co.originX.toFloat() - co.width.toFloat()) / size.toFloat(), (co.originY.toFloat() - co.height.toFloat()) / size.toFloat(), 0f)
			tmat.scale(co.width.toFloat() / size.toFloat(), co.height.toFloat() / size.toFloat(), 1f)
			shader.setUniform(0, 0, tmat)
			val texShaderMat = Matrix4f().translate(co.x.toFloat() / width.toFloat(), co.y.toFloat() / height.toFloat(), 0f).scale(co.width.toFloat() / width.toFloat(), co.height / height.toFloat(), 1f)
			shader.setUniform(0, 1, texShaderMat)
			vao.drawTriangles()
		}
	}
}