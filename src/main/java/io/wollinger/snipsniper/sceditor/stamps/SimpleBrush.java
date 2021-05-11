package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SimpleBrush implements IStamp {
    private final SCEditorWindow scEditorWindow;

    private int size;
    private int speed;

    private PBRColor color;
    private final Config config;

    public SimpleBrush(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
        this.config = scEditorWindow.getConfig();
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
    public Rectangle render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Double[] difference = scEditorWindow.getDifferenceFromImage();
        Vector2Int mousePos = scEditorWindow.getPointOnImage(new Point(input.getMouseX(), input.getMouseY()));

        int newSize = (int) ((double)size * difference[0]);

        Color oldColor = g.getColor();
        g.setColor(new Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor().getBlue(), 255));
        g.fillOval(mousePos.x-newSize/2, mousePos.y-newSize/2, newSize, newSize);
        g.setColor(oldColor);

        Vector2Int p0Temp = scEditorWindow.getPointOnImage(input.getMousePathPoint(0));
        Vector2Int p1Temp = scEditorWindow.getPointOnImage(input.getMousePathPoint(1));

        Point p0 = null;
        Point p1 = null;

        if(p0Temp != null)
            p0 = p0Temp.toPoint();

        if(p1Temp != null)
            p1 = p1Temp.toPoint();

        if(p0 != null && p1 != null) {
            Graphics2D g2 = (Graphics2D)scEditorWindow.getImage().getGraphics();
            g2.setRenderingHints(scEditorWindow.getQualityHints());
            Stroke oldStroke = g2.getStroke();
            oldColor = g2.getColor();
            g2.setColor(new Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor().getBlue(), 255));
            g2.setStroke(new BasicStroke(newSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            double distance = Math.hypot(p0.getX() - p1.getX(), p0.getY() - p1.getY());
            if(distance > config.getInt("editorStampSimpleBrushDistance")) {
                g2.drawLine((int) p0.getX(), (int) p0.getY(), (int) p1.getX(), (int) p1.getY());
                input.removeMousePathPoint(0);
            } else {
                input.removeMousePathPoint(1);
            }

            g2.setStroke(oldStroke);
            g.setColor(oldColor);
            g2.dispose();
        }
        return new Rectangle(mousePos.x-newSize/2, mousePos.y-newSize/2, newSize, newSize);
    }

    @Override
    public void editorUndo(int historyPoint) {

    }

    @Override
    public void reset() {
        color = new PBRColor(config.getColor("editorStampSimpleBrushDefaultColor"));
        size = config.getInt("editorStampSimpleBrushSize");
        speed = config.getInt("editorStampSimpleBrushSizeSpeed");
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
    public void setColor(PBRColor color) {
        this.color = color;
    }

    @Override
    public PBRColor getColor() {
        return color;
    }
}
