package net.snipsniper.snipscope

import net.snipsniper.utils.InputContainer
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Point
import java.awt.event.*

open class SnipScopeListener(private val snipScopeWindow: SnipScopeWindow): KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private val input: InputContainer = snipScopeWindow.inputContainer
    private var lastPoint: Point? = null

    init {
        //TODO: Why is this here? Can we move this?
        snipScopeWindow.addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) {
                snipScopeWindow.resizeTrigger()
            }
        })
    }

    //Key Listener

    override fun keyTyped(keyEvent: KeyEvent) { }

    override fun keyPressed(keyEvent: KeyEvent) {
        input.setKey(keyEvent.keyCode, true)
        if(!snipScopeWindow.isEnableInteraction) return
        when(keyEvent.keyCode) {
            KeyEvent.VK_R -> snipScopeWindow.resetZoom()
            KeyEvent.VK_ESCAPE -> snipScopeWindow.dispose() //TODO: Make this optional or something
        }
        snipScopeWindow.repaint()
    }

    override fun keyReleased(keyEvent: KeyEvent) {
        input.setKey(keyEvent.keyCode, false)
    }

    //Mouse Listener

    override fun mouseClicked(mouseEvent: MouseEvent) { }

    override fun mousePressed(mouseEvent: MouseEvent) {
        if(!snipScopeWindow.isEnableInteraction) return
        snipScopeWindow.uiComponents.forEach { it.mousePressed(mouseEvent) }
    }

    override fun mouseReleased(mouseEvent: MouseEvent) {
        if(!snipScopeWindow.isEnableInteraction) return
        lastPoint = null
        snipScopeWindow.uiComponents.forEach { it.mouseReleased(mouseEvent) }
    }

    override fun mouseEntered(mouseEvent: MouseEvent) { }

    override fun mouseExited(mouseEvent: MouseEvent) { }

    //Mouse Motion Listener

    override fun mouseDragged(mouseEvent: MouseEvent) {
        if(!snipScopeWindow.isEnableInteraction) return
        snipScopeWindow.uiComponents.forEach { it.mouseDragged(mouseEvent) }

        if (input.isKeyPressed(snipScopeWindow.movementKey)) {
            if(lastPoint == null) lastPoint = mouseEvent.point
            lastPoint?.let {
                val x = lastPoint!!.getX() - mouseEvent.point.getX()
                val y = lastPoint!!.getY() - mouseEvent.point.getY()
                snipScopeWindow.position += CCVector2Int(x, y)
            }
            lastPoint = mouseEvent.point
            snipScopeWindow.repaint()
        }
    }

    override fun mouseMoved(mouseEvent: MouseEvent) {
        if(!snipScopeWindow.isEnableInteraction) return
        snipScopeWindow.uiComponents.forEach { it.mouseMoved(mouseEvent) }
    }

    //Mousewheel Listener

    override fun mouseWheelMoved(mouseWheelEvent: MouseWheelEvent) {
        if(!snipScopeWindow.isEnableInteraction) return

        if(!snipScopeWindow.isRequireMovementKeyForZoom || input.isKeyPressed(snipScopeWindow.movementKey)) {
            val oldZoom = snipScopeWindow.zoom
            when (mouseWheelEvent.wheelRotation) {
                -1 -> snipScopeWindow.zoom = oldZoom + 0.1F
                1 -> if (oldZoom >= 0.2F) snipScopeWindow.zoom = oldZoom - 0.1F
            }
            snipScopeWindow.calculateZoom()
        }
    }
}