package io.wollinger.snipsniper.editorwindow.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.editorwindow.EditorWindow;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CubeStamp implements IStamp{
    private final EditorWindow editor;
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

    public CubeStamp(EditorWindow editor) {
        this.editor = editor;
        this.config = editor.getConfig();
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
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
                } else if (dir.equals("Width")) {
                    width -= idSpeed;
                    if (width <= idMinimum)
                        width = idMinimum;
                }
                break;
            case -1:
                if (dir.equals("Height")) {
                    height += idSpeed;
                } else if (dir.equals("Width")) {
                    width += idSpeed;
                }
                break;
        }
    }

    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        boolean isSmartPixel = config.getBool("smartPixel");
        if(isSmartPixel && isSaveRender && !isCensor) {
            Vector2Int pos = new Vector2Int(input.getMouseX()+width/2, input.getMouseY()+height/2);
            Vector2Int size = new Vector2Int(-width, -height);

            for (int y = 0; y < -size.y; y++) {
                for (int x = 0; x < -size.x; x++) {
                    int posX = pos.x - x;
                    int posY = pos.y - y;
                    if(posX >= 0 && posY >= 0 && posX < editor.getImage().getWidth() && posY < editor.getImage().getHeight()) {
                        Color c = new Color(editor.getImage().getRGB(posX, posY));
                        int total = c.getRed() + c.getGreen() + c.getBlue();
                        int alpha = (int)((205F/765F) * total + 25);
                        Color oC = color.getColor();
                        g.setColor(new Color(oC.getRed(), oC.getGreen(), oC.getBlue(), alpha));
                        g.drawLine(posX, posY, posX, posY);
                    }
                }
            }
        } else {
            Color oldColor = g.getColor();
            if(!isCensor)
                g.setColor(color.getColor());
            else
                g.setColor(editor.getCensorColor());

            if(isSmartPixel && !isCensor)
                g.setColor(new PBRColor(color.getColor(), 150).getColor());

            g.fillRect(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
            g.setColor(oldColor);
        }
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void reset() {
        color = new PBRColor(config.getColor("editorStampCubeDefaultColor"));
        width = config.getInt("editorStampCubeWidth");
        height = config.getInt("editorStampCubeHeight");
        thickness = 0;

        minimumWidth = config.getInt("editorStampCubeWidthMinimum");
        minimumHeight = config.getInt("editorStampCubeHeightMinimum");

        speedWidth = config.getInt("editorStampCubeWidthSpeed");
        speedHeight = config.getInt("editorStampCubeHeightSpeed");
        speed = 0;
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
        return "editorStampCube";
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
