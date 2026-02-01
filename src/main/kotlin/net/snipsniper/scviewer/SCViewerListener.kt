package net.snipsniper.scviewer

import net.snipsniper.snipscope.SnipScopeListener
import net.snipsniper.utils.SnipFileChooser
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent

class SCViewerListener(private val scViewerWindow: SCViewerWindow): SnipScopeListener(scViewerWindow) {
    override fun mousePressed(mouseEvent: MouseEvent) {
        super.mousePressed(mouseEvent)
        if(!scViewerWindow.isEnableInteraction) return
        if(mouseEvent.button == 3) scViewerWindow.dispose()
    }

    override fun mouseReleased(mouseEvent: MouseEvent) {
        super.mouseReleased(mouseEvent)
        if(scViewerWindow.isDefaultImage()) {
            val file = SnipFileChooser.openSystemFileChooser(
                parent = scViewerWindow,
                multiFileSelection = false,
                type = SnipFileChooser.SelectionType.FILES_ONLY,
                fileFilters = listOf(SnipFileChooser.IMAGE_FILTER)
            )?.firstOrNull() ?: return
            scViewerWindow.setImage(file)
        }
    }

    override fun keyPressed(keyEvent: KeyEvent) {
        super.keyPressed(keyEvent)
        if(!scViewerWindow.isEnableInteraction) return
        when (keyEvent.keyCode) {
            KeyEvent.VK_LEFT -> scViewerWindow.slideImage(-1)
            KeyEvent.VK_RIGHT -> scViewerWindow.slideImage(1)
            KeyEvent.VK_ENTER -> scViewerWindow.openEditor()
            KeyEvent.VK_F5 -> scViewerWindow.refreshFolder()
        }
    }
}