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

class CircleStamp(private val config: Config) : IStamp {
    override val id = "editorStampCircle"
    override val type: StampType get() = StampType.CIRCLE

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
    private var speed = 0
    private var _color: CCColor? = null
    override var color: CCColor
        get() = _color!!
        set(value) {
            _color = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private val changeListeners = ArrayList<IStampUpdateListener?>()

    init {
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        if (mouseWheelDirection != 0) {
            if (input!!.isKeyPressed(KeyEvent.VK_B)) {
                when (mouseWheelDirection) {
                    1 -> thickness--
                    -1 -> thickness++
                }
                if (thickness <= 0) thickness = 1
                alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
                return
            }
            var doWidth = true
            var doHeight = true
            var speedToUse = speed
            if (input.isKeyPressed(KeyEvent.VK_CONTROL)) {
                doWidth = false
                speedToUse = speedHeight
            } else if (input.isKeyPressed(KeyEvent.VK_SHIFT)) {
                doHeight = false
                speedToUse = speedWidth
            }
            when (mouseWheelDirection) {
                1 -> {
                    if (doWidth) width -= speedToUse
                    if (doHeight) height -= speedToUse
                }
                -1 -> {
                    if (doWidth) width += speedToUse
                    if (doHeight) height += speedToUse
                }
            }
            if (width <= minimumWidth) width = minimumWidth
            if (height <= minimumHeight) height = minimumHeight
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
        }
    }

    override fun render(g: Graphics, input: InputContainer?, position: CCVector2Int?, difference: Array<Double>, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle {
        val drawWidth = (width.toDouble() * difference[0]).toInt()
        val drawHeight = (height.toDouble() * difference[1]).toInt()
        g as Graphics2D
        val oldStroke = g.stroke
        g.stroke = BasicStroke(thickness.toFloat())
        val oldPaint = g.paint
        val x = position!!.x - drawWidth / 2
        val y = position.y - drawHeight / 2
        g.paint = color.getGradientPaint(drawWidth, drawHeight, x, y)
        val rectangle = Rectangle(x, y, drawWidth, drawHeight)
        g.drawOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height)
        g.paint = oldPaint
        g.stroke = oldStroke
        return rectangle
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    override fun reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampCircleDefaultColor)
        width = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidth)
        height = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeight)
        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidthMinimum)
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeightMinimum)
        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidthSpeed)
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeightSpeed)
        speed = config.getInt(ConfigHelper.PROFILE.editorStampCircleSpeed)
        thickness = config.getInt(ConfigHelper.PROFILE.editorStampCircleThickness)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) = changeListeners.forEach { it?.updated(type) }
    override fun addChangeListener(listener: IStampUpdateListener?): Unit = kotlin.run { changeListeners.add(listener) }
    override fun doAlwaysRender() = false
}