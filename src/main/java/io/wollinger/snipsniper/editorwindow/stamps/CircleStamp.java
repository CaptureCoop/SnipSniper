package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;

import java.awt.*;

public class CircleStamp implements IStamp{
    private int width;
    private int height;
    private int thickness;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;
    private int speed;

    public CircleStamp(Config cfg) {
        width = cfg.getInt("editorStampCircleWidth");
        height = cfg.getInt("editorStampCircleHeight");
        thickness = cfg.getInt("editorStampCircleThickness");

        minimumWidth = cfg.getInt("editorStampCircleWidthMinimum");
        minimumHeight = cfg.getInt("editorStampCircleHeightMinimum");

        speedWidth = cfg.getInt("editorStampCircleWidthSpeed");
        speedHeight = cfg.getInt("editorStampCircleHeightSpeed");
        speed = cfg.getInt("editorStampCircleSpeed");
    }

    @Override
    public void updateSize(InputContainer input, int mouseWheelDirection) {

    }

    public void render(Graphics g, InputContainer input) {

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
