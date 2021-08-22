package org.snipsniper.sceditor.stamps;

import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CircleStamp implements IStamp{
    private int width;
    private int height;
    private int thickness;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;
    private int speed;

    private SSColor color;

    private final Config config;

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
        }
    }

    @Override
    public Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
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
        g2.dispose();
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

    @Override
    public int getWidth() {
        return width;
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
    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public SSColor getColor() {
        return color;
    }
}
