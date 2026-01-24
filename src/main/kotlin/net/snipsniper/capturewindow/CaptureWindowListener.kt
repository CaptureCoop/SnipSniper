package net.snipsniper.capturewindow

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.PointType
import org.capturecoop.defaultdepot.math.Vector2I
import java.awt.Cursor
import java.awt.MouseInfo
import java.awt.event.*
import javax.swing.SwingUtilities

class CaptureWindowListener(private val wnd: CaptureWindow) : KeyListener, MouseListener, MouseMotionListener {
    private val keys = BooleanArray(4096)
    private var startPoint: Vector2I? = null //Mouse position given by event *1
    private var startPointTotal: Vector2I? = null //Mouse position given by MouseInfo.getPointerInfo (Different then the above in some scenarios) *2
    private var cPoint: Vector2I? = null //See *1
    private var cPointTotal: Vector2I? = null //See *2
    private var cPointLive: Vector2I? = null //Live position, cPoint and cPointTotal are only set once dragging mouse. cPointLive is always set

    private var startedCapture = false
    private var stoppedCapture = false
    private var hoverLeft = false
    private var hoverRight = false
    private var hoverTop = false
    private var hoverBottom = false
    private var hoverCenter = false

    //Mouse Motion Listener
    override fun mouseDragged(mouseEvent: MouseEvent) {
        if (!stoppedCapture) {
            cPoint = Vector2I(mouseEvent.point)
        } else if (wnd.isAfterDragEnabled) {
            checkMovement(mouseEvent)
        }
        cPointLive = Vector2I(mouseEvent.point)
        if (wnd.config.getBool(ConfigHelper.PROFILE.afterDragContinuesOoBTrim)) trimArea()
    }

    override fun mouseMoved(mouseEvent: MouseEvent) {
        cPointLive = Vector2I(mouseEvent.point)
        checkMouse()
    }

    //Mouse Listener
    override fun mouseEntered(mouseEvent: MouseEvent) = kotlin.run { cPointLive = Vector2I(mouseEvent.point) }
    override fun mouseExited(mouseEvent: MouseEvent) {}
    override fun mouseClicked(mouseEvent: MouseEvent) {}
    override fun mousePressed(mouseEvent: MouseEvent) {
        if (mouseEvent.button == 1 && !isOnSelection()) {
            startPoint = Vector2I(mouseEvent.point)
            cPoint = Vector2I(mouseEvent.point)
            stoppedCapture = false
            startPointTotal = Vector2I(MouseInfo.getPointerInfo().location)
            if (isPressed(wnd.afterDragHotkey) && wnd.afterDragMode.equals("hold", true))
                wnd.isAfterDragHotkeyPressed = true
            startedCapture = true
        } else if (mouseEvent.button == 3) wnd.sniperInstance.killCaptureWindow()

        if (isOnSelection() && stoppedCapture && wnd.isAfterDragEnabled) checkMouse()
    }

    override fun mouseReleased(mouseEvent: MouseEvent) {
        if (mouseEvent.button == 1) {
            if (stoppedCapture && wnd.isAfterDragEnabled) {
                wnd.calcRectangle().also { rect ->
                    startPoint!!.x = rect.x
                    startPoint!!.y = rect.y
                    cPoint!!.x = rect.width + rect.x
                    cPoint!!.y = rect.height + rect.y
                }
            }
            cPointTotal = Vector2I(cPoint!!)
            SwingUtilities.convertPointToScreen(cPointTotal!!.toPoint(), wnd)
            startPointTotal = startPoint!!
            SwingUtilities.convertPointToScreen(startPointTotal!!.toPoint(), wnd)
            stoppedCapture = true
            if (!wnd.config.getBool(ConfigHelper.PROFILE.afterDragContinuesOoBTrim)) trimArea()
            if (!wnd.isAfterDragEnabled) wnd.capture(saveOverride = false, copyOverride = false, editorOverride = false, enforceOverride = false)
        }
    }

    //Key Listener
    override fun keyPressed(keyEvent: KeyEvent) {
        keys[keyEvent.keyCode] = true
        if (keyEvent.keyCode == KeyEvent.VK_ESCAPE) wnd.sniperInstance.killCaptureWindow()
        if (isPressed(KeyEvent.VK_ENTER) || isPressed(KeyEvent.VK_SPACE)) wnd.capture(saveOverride = false, copyOverride =  false, editorOverride = false, enforceOverride = false)
        if (isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_S))   wnd.capture(saveOverride = true,  copyOverride =  false, editorOverride = false, enforceOverride =  true)
        if (isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_C))   wnd.capture(saveOverride = false, copyOverride =  true,  editorOverride = false, enforceOverride =  true)
        if (isPressed(KeyEvent.VK_CONTROL) && isPressed(KeyEvent.VK_E))   wnd.capture(saveOverride = false, copyOverride =  false, editorOverride = true,  enforceOverride =  true)
    }

    override fun keyReleased(keyEvent: KeyEvent) = kotlin.run { keys[keyEvent.keyCode] = false }

    override fun keyTyped(keyEvent: KeyEvent) {}
    fun isPressed(keyCode: Int) = keys[keyCode]

    fun isPressedOnce(keyCode: Int) = keys[keyCode].also { keys[keyCode] = false }

    fun getStartPoint(type: PointType?) = when (type) {
        PointType.NORMAL -> startPoint
        PointType.TOTAL -> startPointTotal
        else -> { throw Exception("getStartPoint type is live! This should not happen!") }
    }

    fun getCurrentPoint(type: PointType?) = when (type) {
        PointType.NORMAL -> cPoint
        PointType.TOTAL -> cPointTotal
        PointType.LIVE -> cPointLive
        else -> null
    }

    private fun isOnSelection(): Boolean {
        if (cPoint == null) return false
        wnd.config.getInt(ConfigHelper.PROFILE.afterDragDeadzone).also { dz ->
            return wnd.calcRectangle().also { rect ->
                rect.x -= dz
                rect.y -= dz
                rect.width += dz * 2
                rect.height += dz * 2
            }.contains(getCurrentPoint(PointType.LIVE)!!.toPoint())
        }
    }
    //TODO: Honor escape key
    private fun checkMouse() {
        if(!startedCapture || !isOnSelection()) {
            wnd.rootPane.cursor = Cursor(Cursor.DEFAULT_CURSOR)
            return
        }
        val rect = wnd.calcRectangle()
        val livePoint = getCurrentPoint(PointType.LIVE)
        val deadzone = wnd.config.getInt(ConfigHelper.PROFILE.afterDragDeadzone)
        hoverTop = false
        hoverBottom = false
        hoverLeft = false
        hoverRight = false
        val pointYTop = rect.y - livePoint!!.y
        if (pointYTop > -deadzone && pointYTop < deadzone) hoverTop = true

        val pointYBottom = pointYTop + rect.height
        if (pointYBottom > -deadzone && pointYBottom < deadzone) hoverBottom = true

        val pointXLeft = rect.x - livePoint.x
        if (pointXLeft > -deadzone && pointXLeft < deadzone) hoverLeft = true

        val pointXRight = pointXLeft + rect.width
        if (pointXRight > -deadzone && pointXRight < deadzone) hoverRight = true

        var toSet: Cursor? = null
        if (hoverLeft || hoverRight) toSet = Cursor(Cursor.W_RESIZE_CURSOR)
        if (hoverTop || hoverBottom) toSet = Cursor(Cursor.N_RESIZE_CURSOR)
        if (hoverLeft && hoverTop) toSet = Cursor(Cursor.NW_RESIZE_CURSOR)
        if (hoverRight && hoverTop) toSet = Cursor(Cursor.NE_RESIZE_CURSOR)
        if (hoverBottom && hoverLeft) toSet = Cursor(Cursor.SW_RESIZE_CURSOR)
        if (hoverBottom && hoverRight) toSet = Cursor(Cursor.SE_RESIZE_CURSOR)
        if (!hoverTop && !hoverBottom && !hoverLeft && !hoverRight) {
            hoverCenter = true
            wnd.rootPane.cursor = Cursor(Cursor.MOVE_CURSOR)
        } else {
            hoverCenter = false
            wnd.rootPane.cursor = toSet ?: throw Exception("CaptureWindowListener checkMouse: No cursor set!!")
        }

    }

    private fun trimArea() {
        if (startPoint!!.x < 0) startPoint!!.x = TRIM_AREA_CUT_MARGIN
        if (startPoint!!.y < 0) startPoint!!.y = TRIM_AREA_CUT_MARGIN
        if (cPoint!!.x > wnd.screenshotBounds!!.width) cPoint!!.x = wnd.screenshotBounds!!.width - TRIM_AREA_CUT_MARGIN
        if (cPoint!!.y > wnd.screenshotBounds!!.height) cPoint!!.y = wnd.screenshotBounds!!.height - TRIM_AREA_CUT_MARGIN
    }

    private fun checkMovement(mouseEvent: MouseEvent) {
        if (hoverTop) startPoint!!.y = cPointLive!!.y
        if (hoverBottom) cPoint!!.y = cPointLive!!.y
        if (hoverLeft) startPoint!!.x = cPointLive!!.x
        if (hoverRight) cPoint!!.x = cPointLive!!.x
        if (hoverCenter) {
            val livePoint = mouseEvent.point
            val moveX = cPointLive!!.x - livePoint.x
            val moveY = cPointLive!!.y - livePoint.y
            startPoint!!.x -= moveX
            startPoint!!.y -= moveY
            cPoint!!.x -= moveX
            cPoint!!.y -= moveY
        }
    }

    fun startedCapture() = startedCapture

    companion object {
        const val TRIM_AREA_CUT_MARGIN = 1 //This makes the outline show up when cutting screenshot off outside the bounds
    }
}