package net.snipsniper.sceditor

import net.snipsniper.utils.DrawUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.getImage
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.swing.*

class SCEditorHistoryWindow(private val editor: SCEditorWindow): JFrame(), CCIClosable {
    private var onClose: ((SCEditorHistoryWindow) -> (Unit))? = null
    private var panel = JPanel()

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        iconImage = "icons/questionmark.png".getImage()
        title = "Edit History"
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) { close() }
        })
        size = Dimension(512, 756)
        editor.location.also {
            location = Point(it.x + editor.width / 2 - width / 2, it.y + editor.height / 2 - height / 2)
        }
        panel.preferredSize = Dimension(512, 512)
        JScrollPane (panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER).also {
            it.verticalScrollBar.unitIncrement = 16
            add(it)
        }
        refresh()
        requestFocus()
        isVisible = true
        addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = refresh()
        })
    }

    fun refresh() {
        panel.removeAll()
        var totalHeight = 0
        //panel.layout = GridBagLayout()
        editor.historyManager.forEachIndexed { i, img ->
            HistoryWindowButton(i, img).also {
                Utils.getScaledDimension(img, size).also { dim ->
                    it.preferredSize = dim
                    it.minimumSize = dim
                    it.maximumSize = dim
                    it.size = dim
                    totalHeight += dim.height
                }
                panel.add(it)
            }
        }
        panel.preferredSize = Dimension(panel.width, totalHeight)
        revalidate()
        repaint()
    }

    fun setOnClose(action: ((SCEditorHistoryWindow) -> (Unit))) = kotlin.run { onClose = action }

    override fun close() {
        onClose?.invoke(this)
        dispose()
    }

    class HistoryWindowButton(private val index: Int, private val img: BufferedImage): JButton() {
        override fun paint(g: Graphics) {
            g.drawImage(img, 0, 0, width, height, this)
            val text = "($index)"
            g.color = Color.WHITE
            DrawUtils.drawCenteredString(g, text, Rectangle(0, 0, img.width, img.height), g.font)
        }
    }
}