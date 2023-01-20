package net.snipsniper.utils

import java.awt.Rectangle
import kotlin.math.min
import kotlin.math.max

class RectangleCollection {
    private var bounds: Rectangle = Rectangle(0, 0, 0, 0)

    constructor()

    constructor(rectangle: Rectangle) {
        addRectangle(rectangle)
    }

    constructor(vararg rectangles: Rectangle) {
        addRectangles(*rectangles)
    }

    fun addRectangle(rectangle: Rectangle) {
        if(rectangle.isEmpty) {
            bounds = rectangle
            return
        }

        bounds.x = min(bounds.x, rectangle.x)
        bounds.y = min(bounds.y, rectangle.y)
        bounds.width = max(bounds.width, rectangle.width)
        bounds.height = max(bounds.height, rectangle.height)
    }

    fun addRectangles(vararg rectangles: Rectangle) = rectangles.forEach { addRectangle(it) }

    val x: Int get() = bounds.x
    val y: Int get() = bounds.y
    val width: Int get() = bounds.width
    val height: Int get() = bounds.height
    fun clear() { bounds = Rectangle(0, 0, 0, 0) }
    fun isEmpty(): Boolean = bounds.isEmpty
    fun getBounds(): Rectangle = Utils.fixRectangle(bounds)
}