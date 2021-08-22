package org.snipsniper.sceditor.stamps;

import org.snipsniper.config.Config;
import org.snipsniper.utils.ConfigHelper;
import org.snipsniper.utils.InputContainer;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Int;

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

    private SSColor color;

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

    public Rectangle render(Graphics g_, InputContainer input, Vector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        Graphics2D g = (Graphics2D) g_;
        Rectangle drawnRectangle = null;
        if(isSaveRender && historyPoint != -1) {
            historyPoints.add(historyPoint);
        }

        int drawWidth = (int) ((double)width * difference[0]);
        int drawHeight = (int) ((double)height * difference[1]);

        if(!isCensor) {
            final int x = position.getX() - drawWidth / 2;
            final int y = position.getY() - drawHeight / 2;

            Paint oldFillColor = g.getPaint();
            g.setPaint(color.getGradientPaint(drawWidth, drawHeight, x, y));
            if (solidColor) {
                SSColor colorToUse = new SSColor(color);
                colorToUse.setPrimaryColor(color.getPrimaryColor(), 255);
                colorToUse.setSecondaryColor(color.getSecondaryColor(), 255);
                g.setPaint(colorToUse.getGradientPaint(drawWidth, drawHeight, x, y));
            }
            g.fillOval(x, y, drawWidth, drawHeight);
            g.setPaint(oldFillColor);

            drawnRectangle = new Rectangle(x, y, drawWidth, drawHeight);

            Color oldColor = g.getColor();
            g.setColor(Color.BLACK);
            int h = (int) (drawHeight / fontSizeModifier);
            g.setFont(new Font("TimesRoman", Font.PLAIN, h));
            int w = g.getFontMetrics().stringWidth("" + count);
            g.drawString("" + count, position.getX() - w / 2, position.getY() + h / 3);
            g.setColor(oldColor);

            if (config.getBool(ConfigHelper.PROFILE.editorStampCounterBorderEnabled)) {
                oldColor = g.getColor();
                g.setColor(Color.BLACK);
                Graphics2D g2 = (Graphics2D) g;
                Stroke oldStroke = g2.getStroke();
                g2.setStroke(new BasicStroke(drawHeight / config.getFloat(ConfigHelper.PROFILE.editorStampCounterBorderModifier)));
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
        color = config.getColor(ConfigHelper.PROFILE.editorStampCounterDefaultColor);
        width = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidth);
        height = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeight);

        minimumWidth = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidthMinimum);
        minimumHeight = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeightMinimum);

        speedWidth = config.getInt(ConfigHelper.PROFILE.editorStampCounterWidthSpeed);
        speedHeight = config.getInt(ConfigHelper.PROFILE.editorStampCounterHeightSpeed);
        speed = config.getInt(ConfigHelper.PROFILE.editorStampCounterSpeed);
        fontSizeModifier = config.getFloat(ConfigHelper.PROFILE.editorStampCounterFontSizeModifier);
        solidColor = config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor);
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
    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public SSColor getColor() {
        return color;
    }
}
