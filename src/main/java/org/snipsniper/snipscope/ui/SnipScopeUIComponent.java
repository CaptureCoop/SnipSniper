package org.snipsniper.snipscope.ui;

import org.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SnipScopeUIComponent {

    private Vector2Int position = new Vector2Int(0, 0);
    private Vector2Int size = new Vector2Int(0, 0);

    public void render(Graphics2D g) { }

    public void mouseMoved(MouseEvent mouseEvent) { }

    public void mouseDragged(MouseEvent mouseEvent) { }

    public void mousePressed(MouseEvent mouseEvent) { }

    public void mouseReleased(MouseEvent mouseEvent) { }

    public Vector2Int getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        position.setX(x);
        position.setY(y);
    }

    public Vector2Int getSize() {
        return size;
    }

    public void setSize(int width, int height) {
        size.setX(width);
        size.setY(height);
    }

    public int getWidth() {
        return size.getX();
    }

    public int getHeight() {
        return size.getY();
    }

    public boolean contains(Point point) {
        return new Rectangle(position.getX(), position.getY(), size.getX(), size.getY()).contains(point);
    }
}
