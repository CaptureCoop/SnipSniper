package io.wollinger.snipsniper.utils;

public class InputContainer {
    private boolean[] keys = new boolean[9182];
    private int mouseWheelDirection = 0; //This can only show last direction, not if it stopped or not.
    private int mouseX;
    private int mouseY;

    public void setKey(int keyCode, boolean pressed) {
        keys[keyCode] = pressed;
    }

    public void setMouseWheelDirection(int direction) {
        mouseWheelDirection = direction;
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

    public int getMouseWheelDirection() {
        return mouseWheelDirection;
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

}
