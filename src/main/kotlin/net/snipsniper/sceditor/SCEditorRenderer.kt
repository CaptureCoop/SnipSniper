package net.snipsniper.sceditor

import net.snipsniper.snipscope.SnipScopeRenderer
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDropEvent
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class SCEditorRenderer(private val scEditorWindow: SCEditorWindow): SnipScopeRenderer(scEditorWindow) {
    private var preview: BufferedImage? = null

    init {
        dropTarget = object: DropTarget() {
            override fun drop(evt: DropTargetDropEvent) {
                evt.acceptDrop(DnDConstants.ACTION_COPY)
                val droppedFiles = evt.transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                (droppedFiles[0] as File).also { file ->
                    scEditorWindow.setSaveLocation(file.absolutePath)
                    scEditorWindow.setInClipboard(false)
                    scEditorWindow.refreshTitle()
                    scEditorWindow.setImage(ImageIO.read(file), true, true)
                }
            }
        }
    }

    fun resetPreview() = kotlin.run {
        //We cant just clone the image here for some reason. i dont know why
        preview = BufferedImage(scEditorWindow.image.width, scEditorWindow.image.height, BufferedImage.TYPE_INT_ARGB)
    }

    override fun paint(g: Graphics) {
        g.color = Color(85, 85, 85) //TODO: This should probably be a constant...
        g.fillRect(0,0, width, height)
        super.paint(g)

        if(preview == null) resetPreview()

        //Helper variables
        val wnd = scEditorWindow
        val ic = wnd.inputContainer
        val stmp = wnd.getSelectedStamp()

        val previewGraphics =  preview!!.graphics as Graphics2D
        previewGraphics.setRenderingHints(wnd.qualityHints) //TODO: Make this toggleable per stamp, for example not for rect stamp, but do it for counter stamp ^^ (Also add it to render function of each stamp..)
        previewGraphics.composite = AlphaComposite.getInstance(AlphaComposite.CLEAR)
        previewGraphics.fillRect(0,0, preview!!.width, preview!!.height)
        previewGraphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER)
        stmp.render(previewGraphics, ic, wnd.getPointOnImage(Point(ic.mouseX, ic.mouseY)), wnd.differenceFromImage, false, false, -1)
        previewGraphics.dispose()

        if((!wnd.isPointOnUiComponents(ic.getMousePoint()) && wnd.isEnableInteraction && wnd.isStampVisible) || stmp.doAlwaysRender())
            g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this)

        renderUI(g as Graphics2D)
    }
}