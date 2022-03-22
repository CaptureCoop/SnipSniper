package net.snipsniper.sceditor.stamps;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.sceditor.SCEditorWindow;
import net.snipsniper.utils.InputContainer;

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

    private CCColor color;

    private float fontSizeModifier;
    private int count;
    private boolean solidColor;

    private final ArrayList<Integer> historyPoints = new ArrayList<>();

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

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

            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
        }
    }

    @Override
    public Rectangle render(Graphics g_, InputContainer input, CCVector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
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
                CCColor colorToUse = new CCColor(color);
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
                Stroke oldStroke = g.getStroke();
                g.setStroke(new BasicStroke(drawHeight / config.getFloat(ConfigHelper.PROFILE.editorStampCounterBorderModifier)));
                g.drawOval(x, y, drawWidth, drawHeight);
                g.setStroke(oldStroke);
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
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
        }
    }

    @Override
    public void mousePressedEvent(int button, boolean pressed) {

    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
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
        return "editorStampCounter";
    }

    @Override
    public void setColor(CCColor color) {
        this.color = color;
        alertChangeListeners(IStampUpdateListener.TYPE.SETTER);
    }

    @Override
    public CCColor getColor() {
        return color;
    }

    @Override
    public StampType getType() {
        return StampType.COUNTER;
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
