package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;

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

    public CircleStamp(Config cfg) {
        width = cfg.getInt("editorStampCircleWidth");
        height = cfg.getInt("editorStampCircleHeight");
        thickness = cfg.getInt("editorStampCircleThickness");

        minimumWidth = cfg.getInt("editorStampCircleWidthMinimum");
        minimumHeight = cfg.getInt("editorStampCircleHeightMinimum");

        speedWidth = cfg.getInt("editorStampCircleWidthSpeed");
        speedHeight = cfg.getInt("editorStampCircleHeightSpeed");
        speed = cfg.getInt("editorStampCircleSpeed");
    }

    @Override
    public void updateSize(InputContainer input, int mouseWheelDirection) {
        if(mouseWheelDirection != 0) {
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

        if(input.isKeyPressed(KeyEvent.VK_PLUS))
            thickness += 1;
        else if(input.isKeyPressed(KeyEvent.VK_MINUS))
            thickness -= 1;

        if(thickness <= 0)
            thickness = 1;

    }

    public void render(Graphics g, InputContainer input) {
        Graphics2D g2 = (Graphics2D) g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        g.drawOval(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
        g2.setStroke(oldStroke);
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
