package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

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

    private final SCEditorWindow scEditorWindow;

    public CircleStamp(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
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
        Double[] difference = scEditorWindow.getDifferenceFromImage();
        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        Vector2Int mousePos = scEditorWindow.getPointOnImage(new Point(input.getMouseX(), input.getMouseY()));
        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(thickness));
        Color oldColor = g2.getColor();
        g2.setColor(color.getColor());
        Rectangle rectangle = new Rectangle(mousePos.getX() - drawWidth / 2, mousePos.getY() - drawHeight / 2, drawWidth, drawHeight);
        g2.drawOval(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
        g2.dispose();
        return rectangle;
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void reset() {
        Config config = scEditorWindow.getConfig();
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
