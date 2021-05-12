package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface IStamp {
    void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent);
    Rectangle render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint);
    void editorUndo(int historyPoint);

    void reset();

    int getWidth();
    int getHeight();

    String getID();

    void setColor(PBRColor color);
    PBRColor getColor();
}
