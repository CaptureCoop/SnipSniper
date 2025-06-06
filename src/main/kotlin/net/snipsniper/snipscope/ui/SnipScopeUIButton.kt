package net.snipsniper.snipscope.ui

import org.capturecoop.defaultdepot.math.Vector2I
import java.awt.Graphics2D
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage

//TODO: This class is currently unused, but worked in the past, make sure it works if its used again
class SnipScopeUIButton(private val icon: BufferedImage, private val iconHovering: BufferedImage, private val iconPressed: BufferedImage) : SnipScopeUIComponent() {
    private val onPress = ArrayList<(() -> (Unit))>()
    private var isHovering = false
    private var isHeld = false
    private var selected = false
    private var lastPosition = Vector2I()

    override fun mouseMoved(mouseEvent: MouseEvent): Boolean {
        if (!super.mouseMoved(mouseEvent)) return false
        isHovering = contains(mouseEvent.point)
        lastPosition = Vector2I(mouseEvent.point)
        return true
    }

    override fun mouseDragged(mouseEvent: MouseEvent): Boolean {
        if (!super.mouseDragged(mouseEvent)) return false
        lastPosition = Vector2I(mouseEvent.point)
        return true
    }

    override fun mousePressed(mouseEvent: MouseEvent?): Boolean {
        if (!super.mousePressed(mouseEvent)) return false
        if (isHovering) isHeld = true
        return true
    }

    override fun mouseReleased(mouseEvent: MouseEvent?): Boolean {
        if (!super.mouseReleased(mouseEvent)) return false
        if (isHeld) {
            isHeld = false
            if (contains(lastPosition.toPoint())) {
                for (function in onPress) {
                    function.invoke()
                }
            }
        }
        return true
    }

    override fun render(g: Graphics2D): Boolean {
        if (!super.render(g)) return false
        var toRender = icon
        if (isHeld || selected) toRender = iconPressed else if (isHovering) toRender = iconHovering
        g.drawImage(toRender, position.x, position.y, width, height, null)
        return true
    }

    fun setSelected(bool: Boolean) {
        selected = bool
    }

    fun addOnPress(function: () -> (Unit)) {
        onPress.add(function)
    }
}