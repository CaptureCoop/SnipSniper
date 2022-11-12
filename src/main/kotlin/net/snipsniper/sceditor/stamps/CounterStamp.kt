package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.*
import java.awt.event.KeyEvent

class CounterStamp(private val config: Config) : IStamp {
    private var minimumWidth = 0
    private var minimumHeight = 0
    private var speedWidth = 0
    private var speedHeight = 0
    private var speed = 0
    override var color: CCColor? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private var fontSizeModifier = 0f
    private var count = 0
    private var solidColor = false
    private val historyPoints = ArrayList<Int>()
    private val changeListeners = ArrayList<IStampUpdateListener?>()
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

    init {
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        if (mouseWheelDirection != 0) {
            var doWidth = true
            var doHeight = true
            var speedToUse = speed
            if (input!!.isKeyPressed(KeyEvent.VK_CONTROL)) {
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

    override fun render(g: Graphics, input: InputContainer?, position: CCVector2Int?, difference: Array<Double>, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle? {
        g as Graphics2D
        var drawnRectangle: Rectangle? = null
        if (isSaveRender && historyPoint != -1) {
            historyPoints.add(historyPoint)
        }
        val drawWidth = (width.toDouble() * difference[0]).toInt()
        val drawHeight = (height.toDouble() * difference[1]).toInt()
        if (!isCensor) {
            val x = position!!.x - drawWidth / 2
            val y = position.y - drawHeight / 2
            val oldFillColor = g.paint
            g.paint = color!!.getGradientPaint(drawWidth, drawHeight, x, y)
            if (solidColor) {
                val colorToUse = CCColor(color)
                colorToUse.setPrimaryColor(color!!.primaryColor, 255)
                colorToUse.setSecondaryColor(color!!.secondaryColor, 255)
                g.paint = colorToUse.getGradientPaint(drawWidth, drawHeight, x, y)
            }
            g.fillOval(x, y, drawWidth, drawHeight)
            g.paint = oldFillColor
            drawnRectangle = Rectangle(x, y, drawWidth, drawHeight)
            var oldColor = g.color
            g.color = Color.BLACK
            val h = (drawHeight / fontSizeModifier).toInt()
            g.font = Font("TimesRoman", Font.PLAIN, h)
            val w = g.fontMetrics.stringWidth("" + count)
            g.drawString("" + count, position.x - w / 2, position.y + h / 3)
            g.color = oldColor
            if (config.getBool(ConfigHelper.PROFILE.editorStampCounterBorderEnabled)) {
                oldColor = g.color
                g.color = Color.BLACK
                val oldStroke = g.stroke
                g.stroke =
                    BasicStroke(drawHeight / config.getFloat(ConfigHelper.PROFILE.editorStampCounterBorderModifier))
                g.drawOval(x, y, drawWidth, drawHeight)
                g.stroke = oldStroke
                g.color = oldColor
            }
        }
        if (isSaveRender) count++
        return drawnRectangle
    }

    override fun editorUndo(historyPoint: Int) {
        if (historyPoints.contains(historyPoint)) {
            for (i in historyPoints.indices) {
                if (historyPoints[i] == historyPoint) {
                    historyPoints.removeAt(i)
                    break
                }
            }
            if (count > 1) count--
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
        }
    }

    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
    }

    override fun reset() {
        count = 1
        color = config.getColor(ConfigHelper.PROFILE.editorStampCounterDefaultColor)
        width = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidth)
        height = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeight)
        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidthMinimum)
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeightMinimum)
        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidthSpeed)
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeightSpeed)
        speed = config.getInt(ConfigHelper.PROFILE.editorStampCounterSpeed)
        fontSizeModifier = config.getFloat(ConfigHelper.PROFILE.editorStampCounterFontSizeModifier)
        solidColor = config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor)
    }

    override val id = "editorStampCounter"

    override val type = StampType.COUNTER

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    override fun doAlwaysRender() = false
}