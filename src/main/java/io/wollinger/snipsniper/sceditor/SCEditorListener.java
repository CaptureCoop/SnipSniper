package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeListener;

import java.awt.event.MouseEvent;

public class SCEditorListener extends SnipScopeListener {
    private SCEditorWindow scEditorWindow;

    public SCEditorListener(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
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
