package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

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

    private PBRColor color;

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
    public Rectangle render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        Color oldColor = g2.getColor();
        g2.setColor(color.getColor());
        g2.drawOval(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
        g2.dispose();
        return null;
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void reset() {
        color = new PBRColor(config.getColor("editorStampCircleDefaultColor"));
        width = config.getInt("editorStampCircleWidth");
        height = config.getInt("editorStampCircleHeight");

        minimumWidth = config.getInt("editorStampCircleWidthMinimum");
        minimumHeight = config.getInt("editorStampCircleHeightMinimum");

        speedWidth = config.getInt("editorStampCircleWidthSpeed");
        speedHeight = config.getInt("editorStampCircleHeightSpeed");
        speed = config.getInt("editorStampCircleSpeed");
        thickness = config.getInt("editorStampCircleThickness");
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
    public void setColor(PBRColor color) {
        this.color = color;
    }

    @Override
    public PBRColor getColor() {
        return color;
    }
}
