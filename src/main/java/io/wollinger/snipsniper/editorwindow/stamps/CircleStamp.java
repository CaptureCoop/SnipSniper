package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.editorwindow.EditorWindow;
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

    private EditorWindow editorWindow;
    private Config config;

    public CircleStamp(EditorWindow editorWindow, Config config) {
        this.editorWindow = editorWindow;
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
    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {

        int x = input.getMouseX();
        int y = input.getMouseY();


        /*float differenceWidth = 1;
        float differenceHeight = 1;

        if(isSaveRender) {
            float windowWidth = editorWindow.getEditorWindowRender().getWidth();
            float imageWidth = editorWindow.getImage().getWidth();

            differenceWidth = imageWidth / windowWidth;

            x = (int)(x * differenceWidth);

            float windowHeight = editorWindow.getEditorWindowRender().getHeight();
            float imageHeight = editorWindow.getImage().getHeight();

            differenceHeight = imageHeight / windowHeight;

            y = (int)(y * differenceHeight);
        }*/


        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        Color oldColor = g2.getColor();
        g2.setColor(color.getColor());
        g2.drawOval(x - width / 2, y - height / 2, /*(int)(*/width/* * (differenceWidth/2))*/, /*(int)(*/height/* * (differenceHeight/2))*/);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
        g2.dispose();
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
