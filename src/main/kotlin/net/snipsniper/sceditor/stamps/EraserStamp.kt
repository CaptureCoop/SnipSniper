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

class EraserStamp(private val scEditorWindow: SCEditorWindow?, private val config: Config) : IStamp {
    private var size = 0
    override var width: Int
        get() = size
        set(value) {
            size = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    override var color: CCColor? = null
    private var speed = 0
    private var pointDistance = 0
    private val changeListeners = ArrayList<IStampUpdateListener?>()
    override var height = 0

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
        if (scEditorWindow != null && input != null && !input.isKeyPressed(scEditorWindow.movementKey)) {
            val p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0))
            val p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1))
            if (p0 != null && p1 != null) {
                val g2 = scEditorWindow.image.graphics as Graphics2D
                g2.setRenderingHints(scEditorWindow.qualityHints)
                if (!input.isKeyPressed(KeyEvent.VK_CONTROL)) {
                    val img = scEditorWindow.originalImage
                    g2.color = Color(0, 0, 0, 0)
                    g2.paint = TexturePaint(img, Rectangle(0, 0, img.width, img.height))
                } else {
                    g2.composite = AlphaComposite.Clear
                }
                g2.stroke = BasicStroke(newSize.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                val distance = hypot((p0.x - p1.x).toDouble(), (p0.y - p1.y).toDouble())
                if (distance > pointDistance) {
                    g2.drawLine(p0.x, p0.y, p1.x, p1.y)
                    input.removeMousePathPoint(0)
                    if (input.getMousePathPoint(1) != null)
                        render(g, input, position, difference, isSaveRender, isCensor, historyPoint)
                } else {
                    input.removeMousePathPoint(1)
                }
                g2.dispose()
            }
        }
        if (!isSaveRender) {
            g.color = Color.WHITE
            g.drawOval(position!!.x - newSize / 2, position.y - newSize / 2, newSize, newSize)
        }
        return Rectangle(position!!.x - newSize / 2, position.y - newSize / 2, newSize, newSize)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    override fun reset() {
        size = config.getInt(ConfigHelper.PROFILE.editorStampEraserSize)
        speed = config.getInt(ConfigHelper.PROFILE.editorStampEraserSizeSpeed)
        pointDistance = config.getInt(ConfigHelper.PROFILE.editorStampEraserDistance)
    }

    override val id = "editorStampEraser"

    override val type = StampType.ERASER

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    override fun doAlwaysRender() = false
}