package net.snipsniper.sceditor

import net.snipsniper.utils.getImage
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.Point
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame

class SCEditorHistoryWindow(private val editor: SCEditorWindow): JFrame(), CCIClosable {
    private var onClose: ((SCEditorHistoryWindow) -> (Unit))? = null

    init {
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        layout = GridBagLayout()
        iconImage = "icons/questionmark.png".getImage()
        title = "Edit History"
        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) { close() }
        })
        size = Dimension(512, 756)
        editor.location.also {
            location = Point(it.x + editor.width / 2 - width / 2, it.y + editor.height / 2 - height / 2)
        }
        isVisible = true
    }

    fun setOnClose(action: ((SCEditorHistoryWindow) -> (Unit))) = kotlin.run { onClose = action }

    override fun close() {
        onClose?.invoke(this)
        dispose()
    }
}