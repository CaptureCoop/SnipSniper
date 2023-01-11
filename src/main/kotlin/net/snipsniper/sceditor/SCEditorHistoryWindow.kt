package net.snipsniper.sceditor

import net.snipsniper.utils.DrawUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.getImage
import org.capturecoop.cclogger.CCLogger
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
        iconImage = "icons/clock.png".getImage()
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
        addComponentListener(object: ComponentAdapter() {
            override fun componentResized(e: ComponentEvent) = refresh()
        })
        isVisible = true
        refresh()
        requestFocus()
        isResizable = false
    }

    fun refresh() {
        CCLogger.debug("refreshing")
        panel.removeAll()
        var totalHeight = 0
        panel.layout = null
        editor.historyManager.history.also { h ->
            var index = h.size - 1
            h.reversed().forEach { img ->
                HistoryWindowButton(index, img).also {
                    Utils.getScaledDimension(img, size).also { dim ->
                        it.preferredSize = dim
                        it.minimumSize = dim
                        it.maximumSize = dim
                        it.size = dim
                    }
                    it.location = Point(0, totalHeight)
                    totalHeight += it.height
                    panel.add(it)
                }
                index--
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
            DrawUtils.drawRect(g, Rectangle(0, 0, width, height))
        }
    }
}