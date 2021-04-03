package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CubeStamp implements IStamp{
    private int width;
    private int height;
    private int thickness;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;
    private int speed;

    public CubeStamp(Config cfg) {
        width = cfg.getInt("editorStampCubeWidth");
        height = cfg.getInt("editorStampCubeHeight");
        thickness = 0;

        minimumWidth = cfg.getInt("editorStampCubeWidthMinimum");
        minimumHeight = cfg.getInt("editorStampCubeHeightMinimum");

        speedWidth = cfg.getInt("editorStampCubeWidthSpeed");
        speedHeight = cfg.getInt("editorStampCubeHeightSpeed");
        speed = 0;
    }

    @Override
    public void updateSize(InputContainer input, int mouseWheelDirection) {
        String dir = "Width";
        if (input.isKeyPressed(KeyEvent.VK_SHIFT))
            dir = "Height";

        int idSpeed = speedWidth;
        int idMinimum = minimumWidth;
        if(dir.equals("Height")) {
            idSpeed = speedHeight;
            idMinimum = minimumHeight;
        }

        switch (mouseWheelDirection) {
            case 1:
                if(dir.equals("Height")) {
                    height -= idSpeed;
                    if(height <= idMinimum)
                        height = idMinimum;
                } else if(dir.equals("Width")) {
                    width -= idSpeed;
                    if(width <= idMinimum)
                        width = idMinimum;
                }
                break;
            case -1:
                if(dir.equals("Height")) {
                    height += idSpeed;
                } else if(dir.equals("Width")) {
                    width += idSpeed;
                }
                break;
        }
    }

    public void render(Graphics g, InputContainer input) {
        g.fillRect(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getMinWidth() {
        return minimumWidth;
    }

    @Override
    public int getMinHeight() {
        return minimumHeight;
    }

    @Override
    public int getSpeedWidth() {
        return speedWidth;
    }

    @Override
    public int getSpeedHeight() {
        return speedHeight;
    }

    @Override
    public int getSpeed() {
        return speed;
    }

    @Override
    public int getThickness() {
        return thickness;
    }
}
