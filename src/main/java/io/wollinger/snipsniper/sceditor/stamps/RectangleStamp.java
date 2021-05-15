package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class RectangleStamp implements IStamp {
    private final SCEditorWindow scEditorWindow;

    private int width;
    private int height;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;

    private int thickness;

    private PBRColor color;

    public RectangleStamp(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
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
    public Rectangle render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Vector2Int mousePos = scEditorWindow.getPointOnImage(new Point(input.getMouseX(), input.getMouseY()));
        Graphics2D g2 = (Graphics2D)g;
        Stroke oldStroke = g2.getStroke();
        Double[] difference = scEditorWindow.getDifferenceFromImage();
        int strokeThickness = (int)(thickness*difference[0]);
        if(strokeThickness <= 0)
            strokeThickness = 1;
        g2.setStroke(new BasicStroke(strokeThickness));
        Color oldColor = g2.getColor();
        g2.setColor(color.getColor());
        Rectangle rectangle = new Rectangle((int)(mousePos.getX() - ((double)width*difference[0]) / 2), (int)(mousePos.getY() - ((double)height*difference[1]) / 2), (int)((double)width*difference[0]), (int)((double)height*difference[1]));
        g2.drawRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
        g2.dispose();
        return rectangle;
    }

    @Override
    public void editorUndo(int historyPoint) { }

    @Override
    public void reset() {
        Config config = scEditorWindow.getConfig();
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
