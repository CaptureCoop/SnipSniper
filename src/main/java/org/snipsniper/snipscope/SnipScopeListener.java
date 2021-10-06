package org.snipsniper.snipscope;

import org.snipsniper.snipscope.ui.SnipScopeUIComponent;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.*;

public class SnipScopeListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
    private final SnipScopeWindow snipScopeWindow;
    private final InputContainer input;
    private Point lastPoint;

    public SnipScopeListener(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
        input = snipScopeWindow.getInputContainer();
        //TODO: Why is this here? Can we move this?
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

        if(!snipScopeWindow.isEnableInteraction()) return;

        switch(keyEvent.getKeyCode()) {
            case KeyEvent.VK_R: snipScopeWindow.resetZoom(); break;
            case KeyEvent.VK_ESCAPE: snipScopeWindow.dispose(); break;
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
    public void mousePressed(MouseEvent mouseEvent) {
        if(!snipScopeWindow.isEnableInteraction()) return;

        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents()) {
            component.mousePressed(mouseEvent);
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if(!snipScopeWindow.isEnableInteraction()) return;

        lastPoint = null;
        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents()) {
            component.mouseReleased(mouseEvent);
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) { }

    @Override
    public void mouseExited(MouseEvent mouseEvent) { }

    //Mouse motion listener

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if(!snipScopeWindow.isEnableInteraction()) return;

        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents()) {
            component.mouseDragged(mouseEvent);
        }

        if (input.isKeyPressed(snipScopeWindow.getMovementKey())) {
            if(lastPoint == null) lastPoint = mouseEvent.getPoint();
            double x = lastPoint.getX() - mouseEvent.getPoint().getX();
            double y = lastPoint.getY() - mouseEvent.getPoint().getY();
            snipScopeWindow.setPosition(Vector2Int.add(snipScopeWindow.getPosition(), new Vector2Int(x, y)));

            lastPoint = mouseEvent.getPoint();
            snipScopeWindow.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {
        if(!snipScopeWindow.isEnableInteraction()) return;

        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents()) {
            component.mouseMoved(mouseEvent);
        }
    }

    //Mouse wheel listener

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        if(!snipScopeWindow.isEnableInteraction()) return;

        if(!snipScopeWindow.isRequireMovementKeyForZoom() || input.isKeyPressed(snipScopeWindow.getMovementKey())) {
            float oldZoom = snipScopeWindow.getZoom();
            switch (mouseWheelEvent.getWheelRotation()) {
                case -1: snipScopeWindow.setZoom(oldZoom + 0.1F); break;
                case 1: if (oldZoom >= 0.2F) snipScopeWindow.setZoom(oldZoom - 0.1F); break;
            }
            snipScopeWindow.calculateZoom();
        }
    }
}
