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
    private final int thickness;

    private final int minimumWidth;
    private final int minimumHeight;

    private final int speedWidth;
    private final int speedHeight;
    private final int speed;

    private PBRColor color = new PBRColor(Color.RED);

    public CubeStamp(EditorWindow editor) {
        this.editor = editor;
        Config cfg = editor.getSniperInstance().cfg;
        width = cfg.getInt("editorStampCubeWidth");
        height = cfg.getInt("editorStampCubeHeight");
        thickness = 0;

        minimumWidth = cfg.getInt("editorStampCubeWidthMinimum");
        minimumHeight = cfg.getInt("editorStampCubeHeightMinimum");

        speedWidth = cfg.getInt("editorStampCubeWidthSpeed");
        speedHeight = cfg.getInt("editorStampCubeHeightSpeed");
        speed = 0;
    }

    @Override
    public void updateSize(InputContainer input, int mouseWheelDirection) {
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
        boolean isSmartPixel = editor.getSniperInstance().cfg.getBool("smartPixel");
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
                        Color oC = color.c;
                        g.setColor(new Color(oC.getRed(), oC.getGreen(), oC.getBlue(), alpha));
                        g.drawLine(posX, posY, posX, posY);
                    }
                }
            }
        } else {
            Color oldColor = g.getColor();
            if(isSmartPixel)
                g.setColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), 150));

            g.fillRect(input.getMouseX() - width / 2, input.getMouseY() - height / 2, width, height);
            g.setColor(oldColor);
        }
    }

    @Override
    public void editorUndo(int historyPoint) {

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
    public void setColor(PBRColor color) {
        this.color = color;
    }

    @Override
    public PBRColor getColor() {
        return color;
    }
}
