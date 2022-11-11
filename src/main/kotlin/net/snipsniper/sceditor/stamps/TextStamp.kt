package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.utils.InputContainer
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.KeyEvent

class TextStamp(private val config: Config, private val scEditorWindow: SCEditorWindow?) : IStamp {
    override var color: CCColor? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    //Returns width in pixels
    override var width: Int
        get() = lastDrawnWidth
        set(_) { }
    override var height: Int
        get() = fontSize
        set(height) {
            fontSize = height
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    private var fontSize = 0
    private var fontSizeSpeed = 0
    var text: String? = null
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
            scEditorWindow?.repaint()
        }
    private val nonTypeKeys = ArrayList<Int>()
    var fontMode = Font.PLAIN
        set(value) {
            field = value
            alertChangeListeners(IStampUpdateListener.TYPE.SETTER)
        }
    var state = TextState.IDLE
        private set
    private var cPosition = CCVector2Int()
    private var livePosition = CCVector2Int()
    private var doSaveNextRender = false

    enum class TextState { IDLE, TYPING }

    private val changeListeners = ArrayList<IStampUpdateListener?>()
    private var lastDrawnWidth = 0

    init {
        nonTypeKeys.add(KeyEvent.VK_SHIFT)
        nonTypeKeys.add(KeyEvent.VK_CONTROL)
        nonTypeKeys.add(KeyEvent.VK_ALT)
        //Arrow keys
        nonTypeKeys.add(KeyEvent.VK_LEFT)
        nonTypeKeys.add(KeyEvent.VK_RIGHT)
        nonTypeKeys.add(KeyEvent.VK_UP)
        nonTypeKeys.add(KeyEvent.VK_DOWN)
        reset()
    }

    override fun update(input: InputContainer?, mouseWheelDirection: Int, keyEvent: KeyEvent?) {
        when (mouseWheelDirection) {
            1 -> fontSize -= fontSizeSpeed
            -1 -> fontSize += fontSizeSpeed
        }
        if (input!!.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_B)) {
            fontMode++
            if (fontMode > 2) fontMode = 0
        }
        if (scEditorWindow!!.isEzMode) {
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
            return
        }
        if (keyEvent != null && state == TextState.TYPING) {
            if (keyEvent.keyCode == KeyEvent.VK_BACK_SPACE) {
                if (text!!.isNotEmpty())
                    text = text!!.substring(0, text!!.length - 1)
                alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
                return
            }
            if (!nonTypeKeys.contains(keyEvent.keyCode) && !input.isKeyPressed(KeyEvent.VK_CONTROL)) text += keyEvent.keyChar
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
    }

    override fun render(g: Graphics, input: InputContainer?, position: CCVector2Int?, difference: Array<Double>, isSaveRender: Boolean, isCensor: Boolean, historyPoint: Int): Rectangle? {
        var inp = input
        if (inp == null) {
            inp = InputContainer()
            inp.setMousePosition(position!!.x, position.y)
        }
        g as Graphics2D
        livePosition = CCVector2Int(inp.mouseX, inp.mouseY) //Update method only gets called upon keypress
        if (isSaveRender && !doSaveNextRender) return null
        val textToDraw = getReadableText()
        val drawFontSize = (fontSize.toDouble() * difference[1]).toInt()
        val oldFont = g.font
        val oldColor = g.paint
        g.font = Font("Arial", fontMode, drawFontSize)
        val width = g.fontMetrics.stringWidth(textToDraw)
        g.paint = color!!.getGradientPaint(width, drawFontSize, position!!.x, position.y)
        lastDrawnWidth = g.fontMetrics.stringWidth(textToDraw)
        g.drawString(textToDraw, position.x - lastDrawnWidth / 2, position.y)
        g.font = oldFont
        g.paint = oldColor
        if (isSaveRender) {
            state = TextState.IDLE
            doSaveNextRender = false
        }
        return Rectangle(position.x, position.y, lastDrawnWidth, drawFontSize)
    }

    override fun editorUndo(historyPoint: Int) {}
    override fun mousePressedEvent(button: Int, pressed: Boolean) {
        if (scEditorWindow!!.isEzMode) {
            doSaveNextRender = true
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
            return
        }
        if (pressed && state == TextState.IDLE) {
            state = TextState.TYPING
            cPosition = CCVector2Int(livePosition)
        } else if (pressed && state == TextState.TYPING) {
            doSaveNextRender = true
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT)
    }

    override fun reset() {
        text = ""
        state = TextState.IDLE
        doSaveNextRender = false
        color = config.getColor(ConfigHelper.PROFILE.editorStampTextDefaultColor)
        fontSize = config.getInt(ConfigHelper.PROFILE.editorStampTextDefaultFontSize)
        fontSizeSpeed = config.getInt(ConfigHelper.PROFILE.editorStampTextDefaultSpeed)
    }

    private fun getReadableText() = if (text == null || text!!.isEmpty()) DEFAULT_TEXT else text!!

    override val id = "editorStampText"

    override val type = StampType.TEXT

    override fun addChangeListener(listener: IStampUpdateListener?) {
        changeListeners.add(listener)
    }

    private fun alertChangeListeners(type: IStampUpdateListener.TYPE) {
        changeListeners.forEach { it?.updated(type) }
    }

    override fun doAlwaysRender() = state != TextState.IDLE

    companion object {
        private const val DEFAULT_TEXT = "Text"
    }
}