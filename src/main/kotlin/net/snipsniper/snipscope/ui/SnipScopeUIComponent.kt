package net.snipsniper.snipscope.ui

import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseEvent

//TODO: This class is currently unused, but worked in the past, make sure it works if its used again
open class SnipScopeUIComponent {
    val position = CCVector2Int(0, 0)
    val size = CCVector2Int(0, 0)
    var isEnabled = true

    open fun render(g: Graphics2D) = isEnabled
    open fun mouseMoved(mouseEvent: MouseEvent) = isEnabled
    open fun mouseDragged(mouseEvent: MouseEvent) = isEnabled
    open fun mousePressed(mouseEvent: MouseEvent?) = isEnabled
    open fun mouseReleased(mouseEvent: MouseEvent?) = isEnabled

    fun setPosition(x: Int, y: Int) {
        position.x = x
        position.y = y
    }

    fun setSize(width: Int, height: Int) {
        size.x = width
        size.y = height
    }

    val width: Int
        get() = size.x
    val height: Int
        get() = size.y

    operator fun contains(point: Point) = if (!isEnabled) false else Rectangle(position.x, position.y, size.x, size.y).contains(point)
}