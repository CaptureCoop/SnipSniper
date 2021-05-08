package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

import java.awt.*;
import java.awt.event.KeyEvent;

public class RectangleStamp implements IStamp {
    private final Config config;

    private int width;
    private int height;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;

    private int thickness;

    private PBRColor color;

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
    }

    @Override
    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        Color oldColor = g2.getColor();
        g2.setColor(color.getColor());
        g2.drawRect(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
        g2.dispose();
    }

    @Override
    public void editorUndo(int historyPoint) { }

    @Override
    public void reset() {
        color = new PBRColor(config.getColor("editorStampRectangleDefaultColor"));

        width = config.getInt("editorStampRectangleWidth");
        height = config.getInt("editorStampRectangleHeight");

        minimumWidth = config.getInt("editorStampRectangleWidthMinimum");
        minimumHeight = config.getInt("editorStampRectangleHeightMinimum");

        speedWidth = config.getInt("editorStampRectangleWidthSpeed");
        speedHeight = config.getInt("editorStampRectangleHeightSpeed");

        thickness = config.getInt("editorStampRectangleThickness");
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
        return "editorStampRectangle";
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
