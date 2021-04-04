package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CircleStamp implements IStamp{
    private int width;
    private int height;
    private int thickness;

    private final int minimumWidth;
    private final int minimumHeight;

    private final int speedWidth;
    private final int speedHeight;
    private final int speed;

    private final float fontSizeModifier;
    private int count = 1;
    private final boolean solidColor;

    public CircleStamp(Config cfg) {
        width = cfg.getInt("editorStampCircleWidth");
        height = cfg.getInt("editorStampCircleHeight");

        minimumWidth = cfg.getInt("editorStampCircleWidthMinimum");
        minimumHeight = cfg.getInt("editorStampCircleHeightMinimum");

        speedWidth = cfg.getInt("editorStampCircleWidthSpeed");
        speedHeight = cfg.getInt("editorStampCircleHeightSpeed");
        speed = cfg.getInt("editorStampCircleSpeed");
        fontSizeModifier = cfg.getFloat("editorStampCircleFontSizeModifier");
        solidColor = cfg.getBool("editorStampCircleSolidColor");
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
    }

    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor) {
        Color oldFillColor = g.getColor();
        if(solidColor) {
            g.setColor(new Color(oldFillColor.getRed(), oldFillColor.getGreen(), oldFillColor.getBlue(), 255));
        }
        g.fillOval(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
        g.setColor(oldFillColor);

        Color oldColor = g.getColor();
        g.setColor(Color.BLACK);

        int h = (int)(height/fontSizeModifier);
        g.setFont(new Font("TimesRoman", Font.PLAIN, h));
        int w = g.getFontMetrics().stringWidth(""+count);
        g.drawString("" + count, input.getMouseX()-w/2, input.getMouseY()+h/3);
        g.setColor(oldColor);

        if(isSaveRender && !isCensor)
            count++;
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
