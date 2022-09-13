package net.snipsniper.utils

import java.awt.Point
import java.util.*
import kotlin.collections.ArrayList

class InputContainer {
    private val keys = BooleanArray(9182)
    private val mousePath = ArrayList<Point>()
    var mouseX = 0
    var mouseY = 0

    fun setMousePosition(mouseX: Int, mouseY: Int) {
        this.mouseX = mouseX
        this.mouseY = mouseY
    }
    fun getMousePoint(): Point = Point(mouseX, mouseY)

    fun addMousePathPoint(point: Point) = mousePath.add(point)
    fun getMousePathPoint(i: Int): Point? = mousePath.getOrNull(i)
    fun removeMousePathPoint(i: Int) = mousePath.removeAt(i)
    fun clearMousePath() = mousePath.clear()

    fun resetKeys() = Arrays.fill(keys, false)
    fun setKey(keyCode: Int, pressed: Boolean) { keys[keyCode] = pressed }
    fun isKeyPressed(keyCode: Int): Boolean = keys[keyCode]
    fun areKeysPressed(vararg keyCodes: Int): Boolean {
        keyCodes.forEach { if(!keys[it]) return false }
        return true
    }
}