package net.snipsniper.sceditor

import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.stamps.TextStamp
import net.snipsniper.snipscope.SnipScopeListener
import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.toBufferedImage
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.cccolorutils.setAlpha
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseWheelEvent
import java.io.File
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFileChooser

class SCEditorListener(private val scEditorWindow: SCEditorWindow): SnipScopeListener(scEditorWindow) {
    private val input = scEditorWindow.inputContainer
    private var openColorChooser = false
    private var openSaveAsWindow = false

    override fun keyPressed(keyEvent: KeyEvent) {
        super.keyPressed(keyEvent)

        if(scEditorWindow.inputContainer.areKeysPressed(KeyEvent.VK_CONTROL, KeyEvent.VK_V)) {
            scEditorWindow.saveLocation = ""
            scEditorWindow.inClipboard = true
            scEditorWindow.refreshTitle()
            //TODO: Check if null and tell user if paste is bad
            scEditorWindow.setImage(ImageUtils.getImageFromClipboard(), resetHistory = true, isNewImage = true)
        }

        if(!scEditorWindow.isEnableInteraction) return

        if(input.isKeyPressed(KeyEvent.VK_PERIOD))
            scEditorWindow.ezMode = !scEditorWindow.ezMode

        var textState = TextStamp.TextState.TYPING
        scEditorWindow.stamps.forEach {
            if(it is TextStamp)
                textState = it.state
        }

        if(input.isKeyPressed(KeyEvent.VK_C) && textState == TextStamp.TextState.IDLE)
            openColorChooser = true

        if(input.isKeyPressed(KeyEvent.VK_ENTER))
            openSaveAsWindow = true

        when (keyEvent.keyCode) {
            KeyEvent.VK_1 -> scEditorWindow.setSelectedStamp(0)
            KeyEvent.VK_2 -> scEditorWindow.setSelectedStamp(1)
            KeyEvent.VK_3 -> scEditorWindow.setSelectedStamp(2)
            KeyEvent.VK_4 -> scEditorWindow.setSelectedStamp(3)
            KeyEvent.VK_5 -> scEditorWindow.setSelectedStamp(4)
            KeyEvent.VK_6 -> scEditorWindow.setSelectedStamp(5)
            KeyEvent.VK_7 -> scEditorWindow.setSelectedStamp(6)
        }

        scEditorWindow.getSelectedStamp().update(scEditorWindow.inputContainer, 0, keyEvent)
        scEditorWindow.repaint()
    }

    override fun keyReleased(keyEvent: KeyEvent) {
        super.keyReleased(keyEvent)

        if(!scEditorWindow.isEnableInteraction) return

        if(openColorChooser) {
            //This fixes an issue with the ALT key getting "stuck" since the key up event is not being received if the color window is in the front.
            openColorChooser = false
            scEditorWindow.inputContainer.resetKeys()
            val wnd = CCColorChooser(scEditorWindow.getSelectedStamp().color!!, "Marker color", parent = scEditorWindow, useGradient = true)
            //TODO: Do we want the save button back?
            scEditorWindow.cWindows.add(wnd)
        }

        if(openSaveAsWindow) {
            openSaveAsWindow = false
            scEditorWindow.inputContainer.resetKeys()
            JFileChooser().also { chooser ->
                chooser.selectedFile = File(Utils.constructFilename(scEditorWindow.config.getString(ConfigHelper.PROFILE.saveFormat), SCEditorWindow.FILENAME_MODIFIER))
                if(chooser.showSaveDialog(chooser) == JFileChooser.APPROVE_OPTION){
                    if(chooser.selectedFile.createNewFile()) ImageIO.write(scEditorWindow.image, "png", chooser.selectedFile)
                }
            }
        }
    }

    override fun mousePressed(mouseEvent: MouseEvent) {
        super.mousePressed(mouseEvent)
        if(!scEditorWindow.isEnableInteraction) return

        if(!scEditorWindow.isPointOnUiComponents(mouseEvent.point))
            scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.button, true)

        if(mouseEvent.button == 3) scEditorWindow.save(true)

        scEditorWindow.repaint()
    }

    override fun mouseReleased(mouseEvent: MouseEvent) {
        super.mouseReleased(mouseEvent)

        if(scEditorWindow.isDefaultImage()) {
            JFileChooser().also {
                if(it.showOpenDialog(scEditorWindow) == JFileChooser.APPROVE_OPTION)
                    scEditorWindow.setImage(ImageIcon(it.selectedFile.absolutePath).image.toBufferedImage(), resetHistory = true, isNewImage = true)
            }
        }

        if(!scEditorWindow.isEnableInteraction) return

        if (!scEditorWindow.isPointOnUiComponents(mouseEvent.point)) {
            scEditorWindow.getSelectedStamp().mousePressedEvent(mouseEvent.button, false)

            scEditorWindow.inputContainer.clearMousePath()
            if (!scEditorWindow.inputContainer.isKeyPressed(scEditorWindow.movementKey)) {
                when (mouseEvent.button) {
                    1 -> save(scEditorWindow.image.graphics, false)
                    2 -> save(scEditorWindow.image.graphics, true)
                    else -> {}
                }
            }
        }

        scEditorWindow.repaint()
        scEditorWindow.requestFocus()
    }

    fun save(g: Graphics, censor: Boolean) {
        scEditorWindow.isDirty = true
        g as Graphics2D
        g.setRenderingHints(scEditorWindow.qualityHints)
        scEditorWindow.getSelectedStamp().render(g, scEditorWindow.inputContainer, scEditorWindow.getPointOnImage(Point(input.mouseX, input.mouseY)), scEditorWindow.differenceFromImage, true, censor, scEditorWindow.historyManager.size)
        scEditorWindow.repaint()
        scEditorWindow.historyManager.addHistory()
    }

    override fun mouseWheelMoved(mouseWheelEvent: MouseWheelEvent) {
        super.mouseWheelMoved(mouseWheelEvent)
        if(!scEditorWindow.isEnableInteraction) return

        val input = scEditorWindow.inputContainer

        if(input.isKeyPressed(KeyEvent.VK_ALT) && scEditorWindow.getSelectedStamp().color != null) {
            val stamp = scEditorWindow.getSelectedStamp()
            val oldColor = stamp.color!!.primaryColor
            val alpha = stamp.color!!.primaryColor.alpha
            val hsv = Array(3) { 0.0F }.toFloatArray()
            Color.RGBtoHSB(oldColor.red, oldColor.green, oldColor.blue, hsv)

            (scEditorWindow.config.getFloat(ConfigHelper.PROFILE.hsvColorSwitchSpeed) / 2500).also { speed ->
                when(mouseWheelEvent.wheelRotation) {
                    1 -> hsv[0] += speed
                    -1 -> hsv[0] -= speed
                }
            }

            val newColor = Color.getHSBColor(hsv[0], hsv[1], hsv[2])
            stamp.color!!.primaryColor = newColor.setAlpha(alpha)
            scEditorWindow.repaint()
            return
        }

        if(!input.isKeyPressed(scEditorWindow.movementKey))
            scEditorWindow.getSelectedStamp().update(input, mouseWheelEvent.wheelRotation, null)
        scEditorWindow.repaint()
    }

    override fun mouseMoved(mouseEvent: MouseEvent) {
        super.mouseMoved(mouseEvent)
        if(!scEditorWindow.isEnableInteraction) return

        scEditorWindow.inputContainer.setMousePosition(mouseEvent.x, mouseEvent.y)
        scEditorWindow.repaint()
    }

    override fun mouseDragged(mouseEvent: MouseEvent) {
        super.mouseDragged(mouseEvent)
        if(!scEditorWindow.isEnableInteraction) return

        scEditorWindow.inputContainer.addMousePathPoint(mouseEvent.point)

        scEditorWindow.inputContainer.setMousePosition(mouseEvent.x, mouseEvent.y)
        scEditorWindow.repaint()
    }

    override fun mouseEntered(mouseEvent: MouseEvent) {
        super.mouseEntered(mouseEvent)
        scEditorWindow.isStampVisible = true
        scEditorWindow.repaint()
    }

    override fun mouseExited(mouseEvent: MouseEvent) {
        super.mouseExited(mouseEvent)
        scEditorWindow.isStampVisible = false
        scEditorWindow.repaint()
    }
}