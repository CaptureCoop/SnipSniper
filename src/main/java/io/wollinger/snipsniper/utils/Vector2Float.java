package io.wollinger.snipsniper.utils;

import java.awt.Point;

public class Vector2Float {
    private float x;
    private float y;

    public Vector2Float() {
        x = 0;
        y = 0;
    }

    public Vector2Float(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2Float(Vector2Float vector) {
        x = vector.x;
        y = vector.y;
    }

    public Vector2Float(Point point) {
        x = (float) point.getX();
        y = (float) point.getY();
    }

    public Vector2Float(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    public Vector2Float add(Vector2Float vector) {
        this.x += vector.getX();
        this.y += vector.getY();
        return this;
    }

    public Vector2Float sub(Vector2Float vector) {
        this.x -= vector.getX();
        this.y -= vector.getY();
        return this;
    }

    public static Vector2Float add(Vector2Float vector1, Vector2Float vector2) {
        return new Vector2Float(vector1).add(vector2);
    }

    public static Vector2Float sub(Vector2Float vector1, Vector2Float vector2) {
        return new Vector2Float(vector1).sub(vector2);
    }

    public void limitX(float min, float max) {
        x = Math.min(Math.max(x, min), max);
    }

    public void limitY(float min, float max) {
        y = Math.min(Math.max(y, min), max);
    }

    public void limit(float min, float max) {
        limitX(min, max);
        limitY(min, max);
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getX() {
        return x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getY() {
        return y;
    }

    public Point toPoint() {
        return new Point((int)x, (int)y);
    }

    public String toString() {
        return "Vector2Float(" + x + "/" + y + ")";
    }
}
