package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage

class CubeStamp(private val config: Config, private val scEditorWindow: SCEditorWindow?) : IStamp {
    private var minimumWidth = 0
    private var minimumHeight = 0
    private var speedWidth = 0
    private var speedHeight = 0
    override var color: CCColor? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
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
    private var smartPixelBuffer: BufferedImage? = null
    private val changeListeners = ArrayList<IStampUpdateListener?>()

    init {
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        var dir = "Width"
        if (input!!.isKeyPressed(KeyEvent.VK_SHIFT)) dir = "Height"
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
        val isSmartPixel = config.getBool(ConfigHelper.PROFILE.editorStampCubeSmartPixel)
        val drawWidth = (width.toDouble() * difference[0]).toInt()
        val drawHeight = (height.toDouble() * difference[1]).toInt()
        g as Graphics2D
        if (isSmartPixel && isSaveRender && !isCensor && scEditorWindow != null) {
            val pos = CCVector2Int(position!!.x + drawWidth / 2, position.y + drawHeight / 2)
            val size = CCVector2Int(-drawWidth, -drawHeight)
            if (color!!.isGradient) {
                if (smartPixelBuffer == null || width != smartPixelBuffer!!.width || height != smartPixelBuffer!!.height) {
                    smartPixelBuffer = BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR)
                }
                (smartPixelBuffer!!.graphics as Graphics2D).also { buffer ->
                    buffer.color = Color(0, 0, 0, 0)
                    buffer.fillRect(0, 0, smartPixelBuffer!!.width, smartPixelBuffer!!.height)
                    buffer.paint = color!!.getGradientPaint(smartPixelBuffer!!.width, smartPixelBuffer!!.height)
                    buffer.fillRect(0, 0, smartPixelBuffer!!.width, smartPixelBuffer!!.height)
                    buffer.dispose()
                }
            }

            //TODO: This fix seems like a hack, go over this again
            val gradientXStart = -size.x - 1
            var gradientX = gradientXStart
            var gradientY = -size.y - 1
            for (y in 0 until -size.y) {
                for (x in 0 until -size.x) {
                    val posX = pos.x - x
                    val posY = pos.y - y
                    if (posX >= 0 && posY >= 0 && posX < scEditorWindow.image.width && posY < scEditorWindow.image.height) {
                        val c = Color(scEditorWindow.image.getRGB(posX, posY))
                        val total = c.red + c.green + c.blue
                        val alpha = (205f / 765f * total + 25).toInt()
                        var oC = color!!.primaryColor
                        if (color!!.isGradient) oC = Color(smartPixelBuffer!!.getRGB(gradientX, gradientY))
                        g.color = Color(oC.red, oC.green, oC.blue, alpha)
                        g.drawLine(posX, posY, posX, posY)
                    }
                    gradientX--
                }
                gradientX = gradientXStart
                gradientY--
            }
        } else {
            val oldColor = g.color
            val x = position!!.x - drawWidth / 2
            val y = position.y - drawHeight / 2
            if (!isCensor) g.paint = color!!.getGradientPaint(drawWidth, drawHeight, x, y) else g.color = Color.BLACK //TODO: Add to config
            if (isSmartPixel && !isCensor) {
                CCColor(color).also {
                    it.setPrimaryColor(it.primaryColor, 150)
                    it.setSecondaryColor(it.secondaryColor, 150)
                    g.paint = it.getGradientPaint(drawWidth, drawHeight, x, y)
                }
            }
            g.fillRect(x, y, drawWidth, drawHeight)
            g.color = oldColor
        }
        return Rectangle(position.x - drawWidth / 2, position.y - drawHeight / 2, drawWidth, drawHeight)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
        for (listener in changeListeners) {
            listener!!.updated(type)
        }
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {}
    override fun reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampCubeDefaultColor)
        width = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidth)
        height = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeight)
        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthMinimum)
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightMinimum)
        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthSpeed)
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightSpeed)
    }

    override val id= "editorStampCube"

    override val type = StampType.CUBE

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    override fun doAlwaysRender() = false
}