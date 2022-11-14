package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.BasicStroke
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.KeyEvent

class RectangleStamp(private val config: Config) : IStamp {
    override var width = 0
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    override var height = 0
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    var thickness = 0
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private var minimumWidth = 0
    private var minimumHeight = 0
    private var speedWidth = 0
    private var speedHeight = 0
    override var color: CCColor? = null
        set(value) {
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
            field = value
        }
    private val changeListeners = ArrayList<IStampUpdateListener?>()

    init {
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        if (input!!.isKeyPressed(KeyEvent.VK_B)) {
            when (mouseWheelDirection) {
                1 -> thickness--
                -1 -> thickness++
            }
            if (thickness <= 0) thickness = 1
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
            return
        }
        var dir = "Width"
        if (input.isKeyPressed(KeyEvent.VK_SHIFT)) dir = "Height"
        var idSpeed = speedWidth
        var idMinimum = minimumWidth
        if (dir == "Height") {
            idSpeed = speedHeight
            idMinimum = minimumHeight
        }
        when (mouseWheelDirection) {
            1 -> if (dir == "Height") {
                height -= idSpeed
                if (height <= idMinimum) height = idMinimum
            } else {
                width -= idSpeed
                if (width <= idMinimum) width = idMinimum
            }

            -1 -> if (dir == "Height") {
                height += idSpeed
            } else {
                width += idSpeed
            }
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
    }

    override fun render(g: Graphics, input: InputContainer?, position: CCVector2Int?, difference: Array<Double>, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle {
        g as Graphics2D
        val oldStroke = g.stroke
        var strokeThickness = (thickness * difference[0]).toInt()
        if (strokeThickness <= 0) strokeThickness = 1
        g.stroke = BasicStroke(strokeThickness.toFloat())
        val drawWidth = (width.toDouble() * difference[0]).toInt()
        val drawHeight = (height.toDouble() * difference[1]).toInt()
        val x = position!!.x - drawWidth / 2
        val y = position.y - drawHeight / 2
        val oldColor = g.paint
        g.paint = color?.getGradientPaint(drawWidth, drawHeight, x, y)
        val rectangle = Rectangle(x, y, drawWidth, drawHeight)
        g.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
        g.paint = oldColor
        g.stroke = oldStroke
        return rectangle
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    override fun reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampRectangleDefaultColor)
        width = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidth)
        height = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeight)
        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidthMinimum)
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeightMinimum)
        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidthSpeed)
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeightSpeed)
        thickness = config.getInt(ConfigHelper.PROFILE.editorStampRectangleThickness)
    }

    override val id = "editorStampRectangle"
    override val type = StampType.RECTANGLE

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
    }

    override fun doAlwaysRender() = false
}