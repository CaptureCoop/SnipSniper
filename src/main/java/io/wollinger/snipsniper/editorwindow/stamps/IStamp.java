package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

import java.awt.*;

public interface IStamp {
    void updateSize(InputContainer input, int mouseWheelDirection);
    void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint);
    void editorUndo(int historyPoint);

    int getWidth();
    int getHeight();

    int getMinWidth();
    int getMinHeight();

    int getSpeedWidth();
    int getSpeedHeight();
    int getSpeed();

    int getThickness();

    String getID();

    void setColor(PBRColor color);
    PBRColor getColor();
}
