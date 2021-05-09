package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeListener;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class SCEditorListener extends SnipScopeListener {
    private SCEditorWindow scEditorWindow;

    public SCEditorListener(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        super.keyPressed(keyEvent);

        switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_1: scEditorWindow.setSelectedStamp(0); break;
            case KeyEvent.VK_2: scEditorWindow.setSelectedStamp(1); break;
            case KeyEvent.VK_3: scEditorWindow.setSelectedStamp(2); break;
            case KeyEvent.VK_4: scEditorWindow.setSelectedStamp(3); break;
            case KeyEvent.VK_5: scEditorWindow.setSelectedStamp(4); break;
            case KeyEvent.VK_6: scEditorWindow.setSelectedStamp(5); break;
        }

        if(keyEvent.getKeyCode() == KeyEvent.VK_SHIFT) {
            scEditorWindow.getSelectedStamp().render(scEditorWindow.getImage().getGraphics(), scEditorWindow.getInputContainer(), true, false, -1);
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        super.mouseMoved(mouseEvent);

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);

        scEditorWindow.getInputContainer().setMousePosition(mouseEvent.getX(), mouseEvent.getY());
        scEditorWindow.repaint();
    }
}
