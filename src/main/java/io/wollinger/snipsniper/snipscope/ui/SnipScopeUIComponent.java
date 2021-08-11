package io.wollinger.snipsniper.snipscope.ui;

import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;

public class SnipScopeUIComponent {

    private PositionType positionType = PositionType.PIXEL;
    private Vector2Int size = new Vector2Int(0, 0);

    enum PositionType {PIXEL, PERCENTAGE};

    public SnipScopeUIComponent() {

    }

    public void updateSize(int windowWidth, int windowHeight) {

    }

    public void render (Graphics2D g) {

    }

    public void setPositionType(PositionType type) {
        positionType = type;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public Vector2Int getSize() {
        return size;
    }

    public void setSize(int width, int height) {
        size.setX(width);
        size.setY(height);
    }

    public void setSize(Vector2Int vector2Int) {
        size.setX(vector2Int.getX());
        size.setY(vector2Int.getY());
    }

    public void setSize(Dimension dimension) {
        size.setX((int) dimension.getWidth());
        size.setY((int) dimension.getHeight());
    }

    public Dimension getSizeDimension() {
        return new Dimension(size.getX(), size.getY());
    }

    public int getWidth() {
        return size.getX();
    }

    public int getHeight() {
        return size.getY();
    }

}
