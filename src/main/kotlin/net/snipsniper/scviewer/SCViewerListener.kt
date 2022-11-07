package net.snipsniper.scviewer

import net.snipsniper.snipscope.SnipScopeListener
import java.awt.event.KeyEvent
import java.awt.event.MouseEvent
import javax.swing.JFileChooser

class SCViewerListener(private val scViewerWindow: SCViewerWindow): SnipScopeListener(scViewerWindow) {
    override fun mousePressed(mouseEvent: MouseEvent) {
        super.mousePressed(mouseEvent)
        if(!scViewerWindow.isEnableInteraction) return
        if(mouseEvent.button == 3) scViewerWindow.dispose()
    }

    override fun mouseReleased(mouseEvent: MouseEvent) {
        super.mouseReleased(mouseEvent)
        if(scViewerWindow.isDefaultImage) {
            JFileChooser().also {
                if(it.showOpenDialog(scViewerWindow) == JFileChooser.APPROVE_OPTION)
                    scViewerWindow.setImage(it.selectedFile)
            }
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