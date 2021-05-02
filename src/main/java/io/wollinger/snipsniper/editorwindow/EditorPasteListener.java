package io.wollinger.snipsniper.editorwindow;

import io.wollinger.snipsniper.utils.Utils;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EditorPasteListener implements KeyListener {
    private final EditorWindow editorWindow;

    private final boolean[] keys = new boolean[9182];

    public EditorPasteListener(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) { }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        keys[keyEvent.getKeyCode()] = true;
        if(keys[KeyEvent.VK_CONTROL] && keys[KeyEvent.VK_V]) {
            editorWindow.initImage(Utils.imageToBufferedImage(Utils.getImageFromClipboard()), "from clipboard");
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        keys[keyEvent.getKeyCode()] = false;
    }
}
