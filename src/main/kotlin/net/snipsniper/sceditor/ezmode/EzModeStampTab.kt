package net.snipsniper.sceditor.ezmode

import net.snipsniper.sceditor.SCEditorWindow
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.image.BufferedImage
import javax.swing.JPanel

class EzModeStampTab(private val image: BufferedImage, size: Int, scEditorWindow: SCEditorWindow, stampIndex: Int): JPanel() {
    init {
        preferredSize = Dimension(size, size)
        addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                scEditorWindow.setSelectedStamp(stampIndex)
            }
        })
        addMouseMotionListener(object: MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent?) {
                scEditorWindow.setSelectedStamp(stampIndex)
            }
        })
    }

    override fun paint(g: Graphics) {
        g.drawImage(image, 0, 0, width, height, null)
    }
}