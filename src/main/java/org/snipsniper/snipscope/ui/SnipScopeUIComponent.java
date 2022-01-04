package org.snipsniper.snipscope.ui;

import net.capturecoop.ccmathutils.Vector2Int;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SnipScopeUIComponent {

    private final Vector2Int position = new Vector2Int(0, 0);
    private final Vector2Int size = new Vector2Int(0, 0);
    private boolean enabled = true;

    public boolean render(Graphics2D g) {
        return enabled;
    }

    public boolean mouseMoved(MouseEvent mouseEvent) {
        return enabled;
    }

    public boolean mouseDragged(MouseEvent mouseEvent) {
        return enabled;
    }

    public boolean mousePressed(MouseEvent mouseEvent) {
        return enabled;
    }

    public boolean mouseReleased(MouseEvent mouseEvent) {
        return enabled;
    }

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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean contains(Point point) {
        if(!enabled) return false;
        return new Rectangle(position.getX(), position.getY(), size.getX(), size.getY()).contains(point);
    }
}
