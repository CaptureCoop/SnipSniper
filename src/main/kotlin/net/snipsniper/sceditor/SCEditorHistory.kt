package net.snipsniper.sceditor

import net.snipsniper.utils.clone
import org.capturecoop.cclogger.CCLogger
import java.awt.image.BufferedImage

class SCEditorHistory(private val editor: SCEditorWindow) {
    private val history = ArrayList<BufferedImage>()

    val size: Int
        get() = history.size

    fun resetHistory() {
        CCLogger.debug("History -> reset")
        history.clear()
        addHistory()
        editor.historyWindow?.refresh()
    }

    fun addHistory() {
        CCLogger.debug("History -> add (${history.size})")
        history.add(editor.image.clone())
        editor.historyWindow?.refresh()
    }

    fun undoHistory() {
        var size = history.size
        if(size > 1) {
            size--
            val startSize = size
            history.removeAt(size)
            size--
            CCLogger.debug("History -> undo ($startSize) -> ($size)")
            editor.setImage(history[size].clone(), resetHistory = false, isNewImage = false)
            editor.stamps.forEach { it.editorUndo(history.size) }
        } else {
            CCLogger.debug("History -> undo (Nothing to undo)")
        }
        editor.historyWindow?.refresh()
    }

    fun forEachIndexed(action: (Int, BufferedImage) -> Unit) {
        history.forEachIndexed { index, image -> action.invoke(index, image) }
    }
}