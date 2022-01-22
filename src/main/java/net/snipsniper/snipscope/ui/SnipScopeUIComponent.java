package net.snipsniper.snipscope.ui;

import org.capturecoop.ccutils.math.CCVector2Int;

import java.awt.*;
import java.awt.event.MouseEvent;

public class SnipScopeUIComponent {

    private final CCVector2Int position = new CCVector2Int(0, 0);
    private final CCVector2Int size = new CCVector2Int(0, 0);
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

    public CCVector2Int getPosition() {
        return position;
    }

    public void setPosition(int x, int y) {
        position.setX(x);
        position.setY(y);
    }

    public CCVector2Int getSize() {
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
