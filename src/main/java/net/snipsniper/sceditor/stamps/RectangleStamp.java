package net.snipsniper.sceditor.stamps;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.InputContainer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class RectangleStamp implements IStamp {
    private final Config config;

    private int width;
    private int height;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;

    private int thickness;

    private CCColor color;

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

    public RectangleStamp(Config config) {
        this.config = config;
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        if(input.isKeyPressed(KeyEvent.VK_B)) {
            switch (mouseWheelDirection) {
                case 1:
                    thickness--;
                    break;
                case -1:
                    thickness++;
                    break;
            }
            if(thickness <= 0)
                thickness = 1;
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
            return;
        }

        String dir = "Width";
        if (input.isKeyPressed(KeyEvent.VK_SHIFT))
            dir = "Height";

        int idSpeed = speedWidth;
        int idMinimum = minimumWidth;
        if (dir.equals("Height")) {
            idSpeed = speedHeight;
            idMinimum = minimumHeight;
        }

        switch (mouseWheelDirection) {
            case 1:
                if (dir.equals("Height")) {
                    height -= idSpeed;
                    if (height <= idMinimum)
                        height = idMinimum;
                } else {
                    width -= idSpeed;
                    if (width <= idMinimum)
                        width = idMinimum;
                }
                break;
            case -1:
                if (dir.equals("Height")) {
                    height += idSpeed;
                } else {
                    width += idSpeed;
                }
                break;
        }
        alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
    }

    @Override
    public Rectangle render(Graphics g, InputContainer input, CCVector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        int strokeThickness = (int)(thickness*difference[0]);
        if(strokeThickness <= 0)
            strokeThickness = 1;
        g2.setStroke(new BasicStroke(strokeThickness));

        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        int x = position.getX() - drawWidth / 2;
        int y = position.getY() - drawHeight / 2;

        Paint oldColor = g2.getPaint();
        g2.setPaint(color.getGradientPaint(drawWidth, drawHeight, x, y));
        Rectangle rectangle = new Rectangle(x, y, drawWidth, drawHeight);
        g2.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        g2.setPaint(oldColor);
        g2.setStroke(oldStroke);
        return rectangle;
    }

    @Override
    public void editorUndo(int historyPoint) { }

    @Override
    public void mousePressedEvent(int button, boolean pressed) { }

    @Override
    public void reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampRectangleDefaultColor);

        width = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidth);
        height = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeight);

        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidthMinimum);
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeightMinimum);

        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampRectangleWidthSpeed);
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampRectangleHeightSpeed);

        thickness = config.getInt(ConfigHelper.PROFILE.editorStampRectangleThickness);
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    public int getThickness() {
        return thickness;
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public String getID() {
        return "editorStampRectangle";
    }

    @Override
    public void setColor(CCColor color) {
        this.color = color;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public CCColor getColor() {
        return color;
    }

    @Override
    public StampType getType() {
        return StampType.RECTANGLE;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
    }

    @Override
    public boolean doAlwaysRender() {
        return false;
    }
}
