package net.snipsniper.configwindow.textpreviewwindow

import net.snipsniper.utils.getImage
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import javax.swing.JPanel

class FolderPreviewRenderer(width: Int, height: Int): JPanel() {
    private val folderIcon = "icons/folder.png".getImage()
    lateinit var textPreviewWindow: TextPreviewWindow

    init {
        Dimension(width, height).also {
            preferredSize = it
            minimumSize = it
        }
    }

    override fun paint(g: Graphics) {
        val content = CCStringUtils.formatDateTimeString(textPreviewWindow.text.replace("\\\\", "/"))
        val partsFinal = ArrayList<String>()
        content.split("/").filter { it.trim().isNotEmpty() }.forEach { partsFinal.add(it) }

        g.color = Color.LIGHT_GRAY
        g.fillRect(0, 0, width, height)

        partsFinal.add(0, "Main folder")
        var size = width / (partsFinal.size)
        if(size > height) size = height / (partsFinal.size)
        partsFinal.forEachIndexed { index, s ->
            g.drawImage(folderIcon, index * size, 0, size, size, null)
            g.font = Font("Consolas", Font.PLAIN, size/10)
            g.color = Color.BLACK
            g.drawString(s, (size / 2 - g.fontMetrics.stringWidth(s) / 2) + (size * index), (size/1.8).toInt())
        }
    }
}