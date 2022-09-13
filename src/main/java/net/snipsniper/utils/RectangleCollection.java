package net.snipsniper.utils;

import java.awt.*;

public class RectangleCollection {
    private Rectangle bounds;

    public RectangleCollection() { }

    public RectangleCollection(Rectangle rectangle) {
        bounds = rectangle;
    }

    public RectangleCollection(Rectangle... rectangles) {
        for (Rectangle rect : rectangles) {
            addRectangle(rect);
        }
    }

    public void addRectangle(Rectangle rectangle) {
        if(bounds == null) {
            bounds = rectangle;
            return;
        }

        bounds.x = Math.min(bounds.x, rectangle.x);
        bounds.y = Math.min(bounds.y, rectangle.y);
        bounds.width = Math.max(bounds.width, rectangle.width);
        bounds.height = Math.max(bounds.height, rectangle.height);
    }

    public void addRectangles(Rectangle... rectangles) {
        for(Rectangle rect : rectangles)
            addRectangle(rect);
    }

    public void clear() {
        bounds = null;
    }

    public Rectangle getBounds() {
        if(bounds == null) return null;

        return Utils.Companion.fixRectangle(bounds);
    }

    public int getX() {
        return bounds.x;
    }

    public int getY() {
        return bounds.y;
    }

    public int getWidth() {
        return bounds.width;
    }

    public int getHeight() {
        return bounds.height;
    }

}
