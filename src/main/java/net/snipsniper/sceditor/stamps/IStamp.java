package net.snipsniper.sceditor.stamps;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.utils.InputContainer;

import java.awt.*;
import java.awt.event.KeyEvent;

public interface IStamp {
    void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent);
    Rectangle render(Graphics g, InputContainer input, CCVector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint);
    void editorUndo(int historyPoint);

    void mousePressedEvent(int button, boolean pressed);

    void reset();

    void setWidth(int width);
    int getWidth();
    void setHeight(int height);
    int getHeight();

    String getID();

    void setColor(CCColor color);
    CCColor getColor();
    StampType getType();

    void addChangeListener(IStampUpdateListener listener);
    boolean doAlwaysRender();
}
