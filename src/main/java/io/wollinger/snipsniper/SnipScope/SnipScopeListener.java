package io.wollinger.snipsniper.SnipScope;

import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Int;
import javafx.scene.input.KeyCode;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class SnipScopeListener implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener{
    private SnipScopeWindow snipScopeWindow;

    private Point lastPoint;
    private Point mousePos;

    public SnipScopeListener(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
        snipScopeWindow.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent evt) {
                snipScopeWindow.setOptimalImageDimension(Utils.getScaledDimension(snipScopeWindow.getImage(), snipScopeWindow.getSize()));
                snipScopeWindow.calculateZoom();
            }
        });
    }

    //Key listener

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        snipScopeWindow.getInputContainer().setKey(e.getKeyCode(), true);
        if(e.getKeyCode() == KeyEvent.VK_R) {
            snipScopeWindow.setPosition(new Vector2Int());
            snipScopeWindow.setZoom(1);
            snipScopeWindow.modX = 0;
            snipScopeWindow.modY = 0;
        }
        snipScopeWindow.repaint();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        snipScopeWindow.getInputContainer().setKey(e.getKeyCode(), false);
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
        lastPoint = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    //Mouse motion listener

    @Override
    public void mouseDragged(MouseEvent e) {
        if (snipScopeWindow.getInputContainer().isKeyPressed(KeyEvent.VK_SPACE)) {
            if(lastPoint == null) lastPoint = e.getPoint();
            double x = lastPoint.getX() - e.getPoint().getX();
            double y = lastPoint.getY() - e.getPoint().getY();


            Vector2Int oldPos = snipScopeWindow.getPosition();
            snipScopeWindow.setPosition(new Vector2Int(oldPos.x + x, oldPos.y +y));

            System.out.println(snipScopeWindow.getPosition());

            System.out.println(x + ", " + y);
            lastPoint = e.getPoint();
            snipScopeWindow.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    //Mouse wheel listener

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getWheelRotation() == -1) {
            float oldZoom = snipScopeWindow.getZoom();
            snipScopeWindow.setZoom(oldZoom+0.1F);
        } else if(e.getWheelRotation() == 1) {
            float oldZoom = snipScopeWindow.getZoom();
            if(oldZoom >= 0.5F)
                snipScopeWindow.setZoom(oldZoom-0.1F);
        }
        snipScopeWindow.calculateZoom();

    }
}
