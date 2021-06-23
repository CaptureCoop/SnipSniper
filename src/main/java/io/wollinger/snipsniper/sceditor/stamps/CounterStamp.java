package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.PBRColor;
import io.wollinger.snipsniper.utils.Vector2Int;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class CounterStamp implements IStamp{
    private final Config config;

    private int width;
    private int height;

    private int minimumWidth;
    private int minimumHeight;

    private int speedWidth;
    private int speedHeight;
    private int speed;

    private PBRColor color;

    private float fontSizeModifier;
    private int count;
    private boolean solidColor;

    private final ArrayList<Integer> historyPoints = new ArrayList<>();

    public CounterStamp(Config config) {
        this.config = config;
        reset();
    }

    @Override
    public void update(InputContainer input, int mouseWheelDirection, KeyEvent keyEvent) {
        if(mouseWheelDirection != 0) {
            boolean doWidth = true;
            boolean doHeight = true;

            int speedToUse = speed;

            if (input.isKeyPressed(KeyEvent.VK_CONTROL)) {
                doWidth = false;
                speedToUse = speedHeight;
            } else if (input.isKeyPressed(KeyEvent.VK_SHIFT)) {
                doHeight = false;
                speedToUse = speedWidth;
            }

            switch (mouseWheelDirection) {
                case 1:
                    if (doWidth) width -= speedToUse;
                    if (doHeight) height -= speedToUse;
                    break;
                case -1:
                    if (doWidth) width += speedToUse;
                    if (doHeight) height += speedToUse;
                    break;
            }

            if (width <= minimumWidth)
                width = minimumWidth;

            if (height <= minimumHeight)
                height = minimumHeight;
        }
    }

    public Rectangle render(Graphics g, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Rectangle drawnRectangle = null;
        if(isSaveRender && historyPoint != -1) {
            historyPoints.add(historyPoint);
        }

        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        if(!isCensor) {
            final int x = position.getX() - drawWidth / 2;
            final int y = position.getY() - drawHeight / 2;

            Color oldFillColor = g.getColor();
            g.setColor(color.getColor());
            if (solidColor) {
                g.setColor(new PBRColor(color.getColor(), 255).getColor());
            }
            g.fillOval(x, y, drawWidth, drawHeight);
            g.setColor(oldFillColor);

            drawnRectangle = new Rectangle(x, y, drawWidth, drawHeight);

            Color oldColor = g.getColor();
            g.setColor(Color.BLACK);
            int h = (int) (drawHeight / fontSizeModifier);
            g.setFont(new Font("TimesRoman", Font.PLAIN, h));
            int w = g.getFontMetrics().stringWidth("" + count);
            g.drawString("" + count, position.getX() - w / 2, position.getY() + h / 3);
            g.setColor(oldColor);

            if (config.getBool("editorStampCounterBorderEnabled")) {
                oldColor = g.getColor();
                g.setColor(Color.BLACK);
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(drawHeight / config.getFloat("editorStampCounterBorderModifier")));
                g2.drawOval(x, y, drawWidth, drawHeight);
                g2.setStroke(oldStroke);
                g2.dispose();
                g.setColor(oldColor);
            }
        }

        if(isSaveRender)
            count++;

        return drawnRectangle;
    }

    @Override
    public void editorUndo(int historyPoint) {
        if(historyPoints.contains(historyPoint)) {
            for(int i = 0; i < historyPoints.size(); i++) {
                if (historyPoints.get(i) == historyPoint) {
                    historyPoints.remove(i);
                    break;
                }
            }
            if (count > 1)
                count--;
        }
    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    @Override
    public void reset() {
        count = 1;
        color = new PBRColor(config.getColor("editorStampCounterDefaultColor"));
        width = config.getInt("editorStampCounterWidth");
        height = config.getInt("editorStampCounterHeight");

        minimumWidth = config.getInt("editorStampCounterWidthMinimum");
        minimumHeight = config.getInt("editorStampCounterHeightMinimum");

        speedWidth = config.getInt("editorStampCounterWidthSpeed");
        speedHeight = config.getInt("editorStampCounterHeightSpeed");
        speed = config.getInt("editorStampCounterSpeed");
        fontSizeModifier = config.getFloat("editorStampCounterFontSizeModifier");
        solidColor = config.getBool("editorStampCounterSolidColor");
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
        return "editorStampCounter";
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
