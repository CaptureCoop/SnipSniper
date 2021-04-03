package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;

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
