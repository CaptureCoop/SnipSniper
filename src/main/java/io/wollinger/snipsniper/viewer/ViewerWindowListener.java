package io.wollinger.snipsniper.viewer;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class ViewerWindowListener implements KeyListener {
    private ViewerWindow viewerWindow;

    public ViewerWindowListener(ViewerWindow viewerWindow) {
        this.viewerWindow = viewerWindow;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) { }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if(keyEvent.getKeyCode() == KeyEvent.VK_LEFT) {
            if(!viewerWindow.isLocked())
                viewerWindow.slideImage(-1);
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_RIGHT) {
            if(!viewerWindow.isLocked())
                viewerWindow.slideImage(1);
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_SPACE) {
            viewerWindow.openEditor();
        } else if(keyEvent.getKeyCode() == KeyEvent.VK_F5) {
            viewerWindow.refreshFolder();
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) { }
}
