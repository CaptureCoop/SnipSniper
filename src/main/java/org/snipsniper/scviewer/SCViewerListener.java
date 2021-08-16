package org.snipsniper.scviewer;

import org.snipsniper.snipscope.SnipScopeListener;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SCViewerListener extends SnipScopeListener {
    private final SCViewerWindow scViewerWindow;

    public SCViewerListener(SCViewerWindow snipScopeWindow) {
        super(snipScopeWindow);
        scViewerWindow = snipScopeWindow;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        super.mousePressed(mouseEvent);
        if(mouseEvent.getButton() == 3)
            scViewerWindow.dispose();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT: scViewerWindow.slideImage(-1); break;
            case KeyEvent.VK_RIGHT: scViewerWindow.slideImage(1); break;
            case KeyEvent.VK_ENTER: scViewerWindow.openEditor(); break;
            case KeyEvent.VK_F5: scViewerWindow.refreshFolder(); break;
        }
    }
}
