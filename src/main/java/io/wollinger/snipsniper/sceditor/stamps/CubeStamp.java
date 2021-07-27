package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.ConfigHelper;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.SSColor;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class CubeStamp implements IStamp{
    private final Config config;
    private final SCEditorWindow scEditorWindow;
    private int width;
    private int height;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;

    private SSColor color;

    public CubeStamp(Config config, SCEditorWindow scEditorWindow) {
        this.config = config;
        this.scEditorWindow = scEditorWindow;
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

    public Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        boolean isSmartPixel = config.getBool(ConfigHelper.PROFILE.smartPixel);

        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        if(isSmartPixel && isSaveRender && !isCensor && scEditorWindow != null) {
            Vector2Int pos = new Vector2Int(position.getX() + drawWidth / 2, position.getY() + drawHeight / 2);
            Vector2Int size = new Vector2Int(-drawWidth, -drawHeight);

            for (int y = 0; y < -size.getY(); y++) {
                for (int x = 0; x < -size.getX(); x++) {
                    int posX = pos.getX() - x;
                    int posY = pos.getY() - y;
                    if(posX >= 0 && posY >= 0 && posX < scEditorWindow.getImage().getWidth() && posY < scEditorWindow.getImage().getHeight()) {

                        Color c = new Color(scEditorWindow.getImage().getRGB(posX, posY));
                        int total = c.getRed() + c.getGreen() + c.getBlue();
                        int alpha = (int)((205F/765F) * total + 25);
                        Color oC = color.getPrimaryColor();
                        g.setColor(new Color(oC.getRed(), oC.getGreen(), oC.getBlue(), alpha));
                        g.drawLine(posX, posY, posX, posY);
                    }
                }
            }
        } else {
            Color oldColor = g.getColor();
            if(!isCensor)
                g.setColor(color.getPrimaryColor());
            else
                g.setColor(Color.BLACK); //TODO: Add to config

            if(isSmartPixel && !isCensor)
                g.setColor(new SSColor(color.getPrimaryColor(), 150).getPrimaryColor());

            g.fillRect(position.getX() - drawWidth / 2, position.getY() - drawHeight / 2, drawWidth, drawHeight);
            g.setColor(oldColor);
        }
        return new Rectangle(position.getX() - drawWidth / 2, position.getY() - drawHeight / 2, drawWidth, drawHeight);
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    @Override
    public void reset() {
        color = new SSColor(config.getColor(ConfigHelper.PROFILE.editorStampCubeDefaultColor));
        width = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidth);
        height = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeight);

        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthMinimum);
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightMinimum);

        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthSpeed);
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightSpeed);
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
    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public SSColor getColor() {
        return color;
    }
}
