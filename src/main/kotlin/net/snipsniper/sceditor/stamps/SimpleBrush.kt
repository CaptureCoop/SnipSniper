package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.*
import java.awt.event.KeyEvent
import kotlin.math.hypot

class SimpleBrush(private val config: Config, private val scEditorWindow: SCEditorWindow?) : IStamp {
    private var size = 0
    override var width: Int
        get() = size
        set(value) {
            size = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private var speed = 0
    private val changeListeners = ArrayList<IStampUpdateListener?>()
    override var height = 0
    override var color: CCColor? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    init {
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        if (mouseWheelDirection != 0) {
            when (mouseWheelDirection) {
                1 -> if (size > 1) size -= speed
                -1 -> size += speed
            }
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
        }
    }

    override fun render(g: Graphics, input: InputContainer?, position: CCVector2Int?, difference: Array<Double>, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle {
        val newSize = (size.toDouble() * difference[0]).toInt()
        g as Graphics2D
        val oldColor: Paint = g.color
        var bounds = g.clipBounds
        if (bounds == null && scEditorWindow != null) bounds =
            Rectangle(0, 0, scEditorWindow.image.width, scEditorWindow.image.height)
        val paint = CCColor(color, 255).getGradientPaint(bounds!!.width, bounds.height)
        g.paint = paint
        g.fillOval(position!!.x - newSize / 2, position.y - newSize / 2, newSize, newSize)
        if (scEditorWindow != null && input != null && !input.isKeyPressed(scEditorWindow.movementKey)) {
            val p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0))
            val p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1))
            if (p0 != null && p1 != null) {
                val g2 = scEditorWindow.image.graphics as Graphics2D
                g2.setRenderingHints(scEditorWindow.qualityHints)
                val oldStroke = g2.stroke
                g2.paint = paint
                g2.stroke = BasicStroke(newSize.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                val distance = hypot((p0.x - p1.x).toDouble(), (p0.y - p1.y).toDouble())
                if (distance > config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushDistance)) {
                    g2.drawLine(p0.x, p0.y, p1.x, p1.y)
                    input.removeMousePathPoint(0)
                    if (input.getMousePathPoint(1) != null)
                        render(g, input, position, difference, isSaveRender, isCensor, historyPoint)
                } else {
                    input.removeMousePathPoint(1)
                }
                g2.stroke = oldStroke
                g.paint = oldColor
            }
        }
        return Rectangle(position.x - newSize / 2, position.y - newSize / 2, newSize, newSize)
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    override fun reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor)
        size = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSize)
        speed = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed)
    }

    override val id = "editorStampSimpleBrush"

    override val type = StampType.SIMPLE_BRUSH

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
    }

    override fun doAlwaysRender() = false
}