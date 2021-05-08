package io.wollinger.snipsniper.SnipScope;

import io.wollinger.snipsniper.utils.Utils;

import java.awt.event.*;

public class SnipScopeListener implements KeyListener, MouseListener {
    private SnipScopeWindow snipScopeWindow;

    public SnipScopeListener(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
        snipScopeWindow.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                snipScopeWindow.setOptimalImageDimension(Utils.getScaledDimension(snipScopeWindow.getImage(), snipScopeWindow.getSize()));
            }
        });
    }

    //Key listener

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    //Mouse listener

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
