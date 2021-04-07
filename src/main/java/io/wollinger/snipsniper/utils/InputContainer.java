package io.wollinger.snipsniper.utils;

import java.awt.*;
import java.util.ArrayList;

public class InputContainer {
    private final boolean[] keys = new boolean[9182];
    private int mouseX;
    private int mouseY;

    private final ArrayList<Point> mousePath = new ArrayList<>();

    public void setKey(int keyCode, boolean pressed) {
        keys[keyCode] = pressed;
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public void addMousePathPoint(Point point) {
        mousePath.add(point);
    }

    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

    public boolean areKeysPressed(int... keyCodes) {
        for(int n : keyCodes) {
            if(!keys[n])
                return false;
        }
        return true;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public Point getMousePathPoint(int i) {
        if(mousePath.size() > i)
            return mousePath.get(i);
        return null;
    }

    public void removeMousePathPoint(int i) {
        mousePath.remove(i);
    }

    public void clearMousePath() {
        mousePath.clear();
    }

}
