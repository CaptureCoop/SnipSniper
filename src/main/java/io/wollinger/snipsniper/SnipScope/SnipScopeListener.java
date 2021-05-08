package io.wollinger.snipsniper.SnipScope;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.*;

public class SnipScopeListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
    private final SnipScopeWindow snipScopeWindow;
    private Point lastPoint;

    public SnipScopeListener(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
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
        InputContainer input = snipScopeWindow.getInputContainer();
        input.setKey(keyEvent.getKeyCode(), true);
        if(input.isKeyPressed(KeyEvent.VK_R)) {
            snipScopeWindow.setPosition(new Vector2Int());
            snipScopeWindow.setZoom(1);
            snipScopeWindow.setZoomOffset(new Vector2Int());
        }
        snipScopeWindow.repaint();
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        snipScopeWindow.getInputContainer().setKey(keyEvent.getKeyCode(), false);
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
        if (snipScopeWindow.getInputContainer().isKeyPressed(KeyEvent.VK_SPACE)) {
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
        if(mouseWheelEvent.getWheelRotation() == -1) {
            float oldZoom = snipScopeWindow.getZoom();
            snipScopeWindow.setZoom(oldZoom+0.1F);
        } else if(mouseWheelEvent.getWheelRotation() == 1) {
            float oldZoom = snipScopeWindow.getZoom();
            if(oldZoom >= 0.2F)
                snipScopeWindow.setZoom(oldZoom-0.1F);
        }
        snipScopeWindow.calculateZoom();
    }
}
