package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.utils.InputContainer;

import java.awt.*;

public interface IStamp {
    public void updateSize(InputContainer input, int mouseWheelDirection);
    public void render(Graphics g, InputContainer input, boolean isSaveRender);

    public int getWidth();
    public int getHeight();

    public int getMinWidth();
    public int getMinHeight();

    public int getSpeedWidth();
    public int getSpeedHeight();
    public int getSpeed();

    public int getThickness();
}
