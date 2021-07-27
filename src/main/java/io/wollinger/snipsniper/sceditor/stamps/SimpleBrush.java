package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.*;

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
        }
    }

    @Override
    public Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        int newSize = (int) ((double)size * difference[0]);

        Color oldColor = g.getColor();
        g.setColor(new Color(color.getPrimaryColor().getRed(), color.getPrimaryColor().getGreen(), color.getPrimaryColor().getBlue(), 255));
        g.fillOval(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);
        g.setColor(oldColor);

        if(scEditorWindow != null && !input.isKeyPressed(scEditorWindow.getMovementKey())) {
            Vector2Int p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0));
            Vector2Int p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1));

            if (p0 != null && p1 != null) {
                Graphics2D g2 = (Graphics2D) scEditorWindow.getImage().getGraphics();
                g2.setRenderingHints(scEditorWindow.getQualityHints());
                Stroke oldStroke = g2.getStroke();
                oldColor = g2.getColor();
                g2.setColor(new Color(color.getPrimaryColor().getRed(), color.getPrimaryColor().getGreen(), color.getPrimaryColor().getBlue(), 255));
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
                g.setColor(oldColor);
                g2.dispose();
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
        color = new SSColor(config.getColor(ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor));
        size = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSize);
        speed = config.getInt(ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed);
    }

    @Override
    public int getWidth() {
        return 0;
    }

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
}
