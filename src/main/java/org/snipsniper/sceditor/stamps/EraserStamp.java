package org.snipsniper.sceditor.stamps;

import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class EraserStamp implements IStamp {
    private final SCEditorWindow scEditorWindow;
    private final Config config;

    private int size;
    private int speed;
    private int pointDistance;

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

    public EraserStamp(SCEditorWindow scEditorWindow, Config config) {
        this.scEditorWindow = scEditorWindow;
        this.config = config;
        reset();
    }
    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        if(mouseWheelDirection != 0) {
            switch (mouseWheelDirection) {
                case 1:
                    if(size > 1) size -= speed;
                    break;
                case -1:
                    size += speed;
                    break;
            }
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
        }
    }

    @Override
    public Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        int newSize = (int) ((double)size * difference[0]);

        if(scEditorWindow != null && input != null && !input.isKeyPressed(scEditorWindow.getMovementKey())) {
            Vector2Int p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0));
            Vector2Int p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1));

            if (p0 != null && p1 != null) {
                Graphics2D g2 = (Graphics2D) scEditorWindow.getImage().getGraphics();
                g2.setRenderingHints(scEditorWindow.getQualityHints());
                BufferedImage img = scEditorWindow.getOriginalImage();
                g2.setPaint(new TexturePaint(img, new Rectangle(0, 0, img.getWidth(), img.getHeight())));
                g2.setStroke(new BasicStroke(newSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                double distance = Math.hypot(p0.getX() - p1.getX(), p0.getY() - p1.getY());
                if (distance > pointDistance) {
                    g2.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    input.removeMousePathPoint(0);
                    if(input.getMousePathPoint(1) != null)
                        render(g, input, position, difference, isSaveRender, isCensor, historyPoint);
                } else {
                    input.removeMousePathPoint(1);
                }

                g2.dispose();
            }
        }
        if(!isSaveRender) {
            g.setColor(Color.WHITE);
            g.drawOval(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);
        }
        return new Rectangle(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
    }

    @Override
    public void editorUndo(int historyPoint) { }

    @Override
    public void mousePressedEvent(int button, boolean pressed) { }

    @Override
    public void reset() {
        size = config.getInt(ConfigHelper.PROFILE.editorStampEraserSize);
        speed = config.getInt(ConfigHelper.PROFILE.editorStampEraserSizeSpeed);
        pointDistance = config.getInt(ConfigHelper.PROFILE.editorStampEraserDistance);
    }

    @Override
    public void setWidth(int width) {
        size = width;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public int getWidth() {
        return size;
    }

    @Override
    public void setHeight(int height) { }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public String getID() {
        return "editorStampEraser";
    }

    @Override
    public void setColor(SSColor color) { }

    @Override
    public SSColor getColor() {
        return null;
    }

    @Override
    public StampUtils.TYPE getType() {
        return StampUtils.TYPE.ERASER;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }
}
