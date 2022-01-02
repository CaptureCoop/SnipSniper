package org.snipsniper.sceditor.stamps;

import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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

    private BufferedImage smartPixelBuffer;

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();


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

        alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
    }

    public Rectangle render(Graphics g_, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        boolean isSmartPixel = config.getBool(ConfigHelper.PROFILE.editorStampCubeSmartPixel);

        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        Graphics2D g = (Graphics2D) g_;

        if(isSmartPixel && isSaveRender && !isCensor && scEditorWindow != null) {
            Vector2Int pos = new Vector2Int(position.getX() + drawWidth / 2, position.getY() + drawHeight / 2);
            Vector2Int size = new Vector2Int(-drawWidth, -drawHeight);

            if(color.isGradient()) {
                if(smartPixelBuffer == null || width != smartPixelBuffer.getWidth() || height != smartPixelBuffer.getHeight()) {
                    smartPixelBuffer = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
                }
                Graphics2D smartPixelBufferGraphics = (Graphics2D) smartPixelBuffer.getGraphics();
                smartPixelBufferGraphics.setColor(new Color(0, 0, 0, 0));
                smartPixelBufferGraphics.fillRect(0, 0, smartPixelBuffer.getWidth(), smartPixelBuffer.getHeight());
                smartPixelBufferGraphics.setPaint(color.getGradientPaint(smartPixelBuffer.getWidth(), smartPixelBuffer.getHeight()));
                smartPixelBufferGraphics.fillRect(0, 0, smartPixelBuffer.getWidth(), smartPixelBuffer.getHeight());
                smartPixelBufferGraphics.dispose();
            }

            for (int y = 0; y < -size.getY(); y++) {
                for (int x = 0; x < -size.getX(); x++) {
                    int posX = pos.getX() - x;
                    int posY = pos.getY() - y;
                    if(posX >= 0 && posY >= 0 && posX < scEditorWindow.getImage().getWidth() && posY < scEditorWindow.getImage().getHeight()) {
                        Color c = new Color(scEditorWindow.getImage().getRGB(posX, posY));
                        int total = c.getRed() + c.getGreen() + c.getBlue();
                        int alpha = (int)((205F/765F) * total + 25);
                        Color oC = color.getPrimaryColor();
                        if(color.isGradient())
                            oC = new Color(smartPixelBuffer.getRGB(x, y));
                        g.setColor(new Color(oC.getRed(), oC.getGreen(), oC.getBlue(), alpha));
                        g.drawLine(posX, posY, posX, posY);
                    }
                }
            }
        } else {
            Color oldColor = g.getColor();
            int x = position.getX() - drawWidth / 2;
            int y = position.getY() - drawHeight / 2;
            if(!isCensor)
                g.setPaint(color.getGradientPaint(drawWidth, drawHeight, x, y));
            else
                g.setColor(Color.BLACK); //TODO: Add to config

            if(isSmartPixel && !isCensor) {
                SSColor smartPixelPreview = new SSColor(color);
                smartPixelPreview.setPrimaryColor(smartPixelPreview.getPrimaryColor(), 150);
                smartPixelPreview.setSecondaryColor(smartPixelPreview.getSecondaryColor(), 150);
                g.setPaint(smartPixelPreview.getGradientPaint(drawWidth, drawHeight, x, y));
            }

            g.fillRect(x, y, drawWidth, drawHeight);
            g.setColor(oldColor);
        }
        return new Rectangle(position.getX() - drawWidth / 2, position.getY() - drawHeight / 2, drawWidth, drawHeight);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    @Override
    public void reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampCubeDefaultColor);
        width = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidth);
        height = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeight);

        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthMinimum);
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightMinimum);

        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCubeWidthSpeed);
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCubeHeightSpeed);
    }

    @Override
    public void setWidth(int width) {
        this.width = width;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
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
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public SSColor getColor() {
        return color;
    }

    @Override
    public StampUtils.TYPE getType() {
        return StampUtils.TYPE.CUBE;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }

    @Override
    public boolean doAlwaysRender() {
        return false;
    }
}
