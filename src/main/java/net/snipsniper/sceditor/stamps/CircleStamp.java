package net.snipsniper.sceditor.stamps;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.utils.InputContainer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class CircleStamp implements IStamp{
    private int width;
    private int height;
    private int thickness;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;
    private int speed;

    private CCColor color;

    private final Config config;

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

    public CircleStamp(Config config) {
        this.config = config;
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        if(mouseWheelDirection != 0) {
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

            boolean doWidth = true;
            boolean doHeight = true;

            int speedToUse = speed;

            if (input.isKeyPressed(KeyEvent.VK_CONTROL)) {
                doWidth = false;
                speedToUse = speedHeight;
            } else if (input.isKeyPressed(KeyEvent.VK_SHIFT)) {
                doHeight = false;
                speedToUse = speedWidth;
            }

            switch (mouseWheelDirection) {
                case 1:
                    if (doWidth) width -= speedToUse;
                    if (doHeight) height -= speedToUse;
                    break;
                case -1:
                    if (doWidth) width += speedToUse;
                    if (doHeight) height += speedToUse;
                    break;
            }

            if (width <= minimumWidth)
                width = minimumWidth;

            if (height <= minimumHeight)
                height = minimumHeight;

            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
        }
    }

    @Override
    public Rectangle render(Graphics g, InputContainer input, CCVector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        Paint oldPaint = g2.getPaint();
        int x = position.getX() - drawWidth / 2;
        int y = position.getY() - drawHeight / 2;
        g2.setPaint(color.getGradientPaint(drawWidth, drawHeight, x, y));
        Rectangle rectangle = new Rectangle(x, y, drawWidth, drawHeight);
        g2.drawOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        g2.setPaint(oldPaint);
        g2.setStroke(oldStroke);
        return rectangle;
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    @Override
    public void reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampCircleDefaultColor);
        width = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidth);
        height = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeight);

        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidthMinimum);
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeightMinimum);

        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCircleWidthSpeed);
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCircleHeightSpeed);
        speed = config.getInt(ConfigHelper.PROFILE.editorStampCircleSpeed);
        thickness = config.getInt(ConfigHelper.PROFILE.editorStampCircleThickness);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
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
        return "editorStampCircle";
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
        return StampType.CIRCLE;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public boolean doAlwaysRender() {
        return false;
    }
}
