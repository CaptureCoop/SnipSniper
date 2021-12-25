package org.snipsniper.sceditor.stamps;

import org.snipsniper.LogManager;
import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;
import org.snipsniper.utils.enums.LogLevel;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SimpleBrush implements IStamp {
    private final Config config;
    private final SCEditorWindow scEditorWindow;

    private int size;
    private int speed;

    private SSColor color;

    public SimpleBrush(Config config, SCEditorWindow scEditorWindow) {
        this.config = config;
        this.scEditorWindow = scEditorWindow;
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
            if(scEditorWindow != null)
                scEditorWindow.updateEzUI();
        }
    }

    @Override
    public Rectangle render(Graphics g_, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        int newSize = (int) ((double)size * difference[0]);
        Graphics2D g = (Graphics2D) g_;

        Paint oldColor = g.getColor();
        Rectangle bounds = g.getClipBounds();
        if(bounds == null && scEditorWindow != null)
            bounds = new Rectangle(0, 0, scEditorWindow.getImage().getWidth(), scEditorWindow.getImage().getHeight());

        Paint paint = new SSColor(color, 255).getGradientPaint((int)bounds.getWidth(), (int)bounds.getHeight());
        g.setPaint(paint);
        g.fillOval(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);

        if(scEditorWindow != null && input != null && !input.isKeyPressed(scEditorWindow.getMovementKey())) {
            Vector2Int p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0));
            Vector2Int p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1));

            if (p0 != null && p1 != null) {
                Graphics2D g2 = (Graphics2D) scEditorWindow.getImage().getGraphics();
                g2.setRenderingHints(scEditorWindow.getQualityHints());
                Stroke oldStroke = g2.getStroke();
                g2.setPaint(paint);
                g2.setStroke(new BasicStroke(newSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                double distance = Math.hypot(p0.getX() - p1.getX(), p0.getY() - p1.getY());
                if (distance > config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushDistance)) {
                    g2.drawLine(p0.getX(), p0.getY(), p1.getX(), p1.getY());
                    input.removeMousePathPoint(0);
                    if(input.getMousePathPoint(1) != null)
                        render(g, input, position, difference, isSaveRender, isCensor, historyPoint);
                } else {
                    input.removeMousePathPoint(1);
                }

                g2.setStroke(oldStroke);
                g.setPaint(oldColor);
            }
        }
        return new Rectangle(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    @Override
    public void reset() {
        color = config.getColor(ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor);
        size = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSize);
        speed = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed);
    }

    @Override
    public void setWidth(int width) {
        size = width;
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
        return "editorStampSimpleBrush";
    }

    @Override
    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public SSColor getColor() {
        return color;
    }

    @Override
    public StampUtils.TYPE getType() {
        return StampUtils.TYPE.BRUSH;
    }
}
