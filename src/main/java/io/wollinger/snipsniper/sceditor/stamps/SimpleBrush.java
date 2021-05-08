package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;

import java.awt.*;
import java.awt.event.KeyEvent;

public class SimpleBrush implements IStamp {
    private final SCEditorWindow editorWindow;

    private int size;
    private int speed;

    private PBRColor color;
    private final Config config;

    public SimpleBrush(SCEditorWindow editorWindow) {
        this.editorWindow = editorWindow;
        this.config = editorWindow.getConfig();
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
    public void render(Graphics g, InputContainer input, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Color oldColor = g.getColor();
        g.setColor(new Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor().getBlue(), 255));
        g.fillOval(input.getMouseX()-size/2, input.getMouseY()-size/2, size, size);
        g.setColor(oldColor);

        Point p0 = input.getMousePathPoint(0);
        Point p1 = input.getMousePathPoint(1);
        if(p0 != null && p1 != null) {
            Graphics2D g2 = (Graphics2D)editorWindow.getImage().getGraphics();
            g2.setRenderingHints(editorWindow.getQualityHints());
            Stroke oldStroke = g2.getStroke();
            oldColor = g2.getColor();
            g2.setColor(new Color(color.getColor().getRed(), color.getColor().getGreen(), color.getColor().getBlue(), 255));
            g2.setStroke(new BasicStroke(size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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
