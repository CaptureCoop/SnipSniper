package io.wollinger.snipsniper.snipscope;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.*;

public class SnipScopeListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
    private final SnipScopeWindow snipScopeWindow;
    private final InputContainer input;
    private Point lastPoint;

    public SnipScopeListener(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
        input = snipScopeWindow.getInputContainer();
        snipScopeWindow.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                snipScopeWindow.resizeTrigger();
            }
        });
    }

    //Key listener

    @Override
    public void keyTyped(KeyEvent keyEvent) { }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        input.setKey(keyEvent.getKeyCode(), true);
        if(input.isKeyPressed(KeyEvent.VK_R)) {
            snipScopeWindow.resetZoom();
        }
        snipScopeWindow.repaint();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        input.setKey(keyEvent.getKeyCode(), false);
    }

    //Mouse listener

    @Override
    public void mouseClicked(MouseEvent mouseEvent) { }

    @Override
    public void mousePressed(MouseEvent mouseEvent) { }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        lastPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) { }

    @Override
    public void mouseExited(MouseEvent mouseEvent) { }

    //Mouse motion listener

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (input.isKeyPressed(snipScopeWindow.getMovementKey())) {
            if(lastPoint == null) lastPoint = mouseEvent.getPoint();
            double x = lastPoint.getX() - mouseEvent.getPoint().getX();
            double y = lastPoint.getY() - mouseEvent.getPoint().getY();

            Vector2Int oldPos = snipScopeWindow.getPosition();
            snipScopeWindow.setPosition(new Vector2Int(oldPos.x + x, oldPos.y +y));

            lastPoint = mouseEvent.getPoint();
            snipScopeWindow.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) { }

    //Mouse wheel listener

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        if(input.isKeyPressed(KeyEvent.VK_SPACE)) {
            float oldZoom = snipScopeWindow.getZoom();
            if (mouseWheelEvent.getWheelRotation() == -1) {
                snipScopeWindow.setZoom(oldZoom + 0.1F);
            } else if (mouseWheelEvent.getWheelRotation() == 1) {
                if (oldZoom >= 0.2F)
                    snipScopeWindow.setZoom(oldZoom - 0.1F);
            }
            snipScopeWindow.calculateZoom();
        }
    }
}
