package org.snipsniper.sceditor.stamps;

import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface IStamp {
    void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent);
    Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint);
    void editorUndo(int historyPoint);

    void mousePressedEvent(int button, boolean pressed);

    void reset();

    int getWidth();
    int getHeight();

    String getID();

    void setColor(SSColor color);
    SSColor getColor();
}