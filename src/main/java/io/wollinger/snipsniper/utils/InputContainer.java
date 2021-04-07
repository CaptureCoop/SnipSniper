package io.wollinger.snipsniper.utils;

public class InputContainer {
    private final boolean[] keys = new boolean[9182];
    private int mouseX;
    private int mouseY;

    public void setKey(int keyCode, boolean pressed) {
        keys[keyCode] = pressed;
    }

    public void setMousePosition(int mouseX, int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
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

}
