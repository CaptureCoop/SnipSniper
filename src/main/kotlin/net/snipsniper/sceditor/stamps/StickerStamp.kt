package net.snipsniper.sceditor.stamps

import net.snipsniper.ImageManager
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.utils.InputContainer
import net.snipsniper.utils.Utils
import org.capturecoop.colorcomposer.ComposedColor
import org.capturecoop.defaultdepot.math.Vector2I
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage

class StickerStamp(private val config: Config, private val scEditorWindow: SCEditorWindow?) : IStamp {
    //TODO: This should be an IStamp feature. Use abstract open classes!
    private val changeListeners = ArrayList<IStampUpdateListener?>()
    override var color: ComposedColor? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    override var width = 0 //Calculated dynamically when reset
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    override var height = 128
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private var speedWidth = 0
    private var speedHeight = 0
    private var speed = 0

    var image: BufferedImage = ImageManager.getImage("icons/random/dude.png")

    init {
        reset()
    }

    override fun update(
        input: InputContainer?,
        mouseWheelDirection: Int,
        keyEvent: KeyEvent?
    ) {
        val isShiftPressed = input!!.isKeyPressed(KeyEvent.VK_SHIFT)
        val isControlPressed = input!!.isKeyPressed(KeyEvent.VK_CONTROL)

        //Adapt it to the circle logic im too tired for this right now. Clean all the stamps they are dusty.
        val scaleWidth = isShiftPressed && !isControlPressed
        val scaleHeight = !isShiftPressed && isControlPressed
        val scaleAll = !isShiftPressed && !isControlPressed

        val speedWidth = if(!scaleAll) speedWidth else (speed) * (width / height.toFloat()).toInt()
        val speedHeight = if(!scaleAll) speedHeight else (speed)

        when(mouseWheelDirection) {
            -1 -> {
                if(scaleAll || scaleWidth) width += speedWidth
                if(scaleAll || scaleHeight) height += speedHeight
            }
            1 -> {
                if(scaleAll || scaleWidth) width -= speedWidth
                if(scaleAll || scaleHeight) height -= speedHeight
            }
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
    }

    override fun render(
        g: Graphics,
        input: InputContainer?,
        position: Vector2I?,
        difference: Array<Double>,
        isSaveRender: Boolean,
        isCensor: Boolean,
        historyPoint: Int
    ): Rectangle? {
        g as Graphics2D
        g.setRenderingHints(Utils.getPixelatedRenderingHints())
        g.drawImage(image, position!!.x - width / 2, position!!.y - height / 2, width, height, null)
        return Rectangle()
    }

    override fun editorUndo(historyPoint: Int) { }
    override fun mousePressedEvent(button: Int, pressed: Boolean) { }

    override fun reset() {
        height = config.getInt(ConfigHelper.PROFILE.editorStampStickerHeight)
        width = (height  * (image.width / image.height.toFloat())).toInt()
        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampStickerWidthSpeed)
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampStickerHeightSpeed)
        speed = config.getInt(ConfigHelper.PROFILE.editorStampStickerSpeed)
    }

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    override fun doAlwaysRender(): Boolean = false

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
        for (listener in changeListeners) {
            listener!!.updated(type)
        }
    }

    override val id= "editorStampSticker"

    override val type = StampType.STICKER
}