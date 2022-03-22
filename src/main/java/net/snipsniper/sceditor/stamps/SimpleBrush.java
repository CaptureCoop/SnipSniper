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

public class SimpleBrush implements IStamp {
    private final Config config;
    private final SCEditorWindow scEditorWindow;

    private int size;
    private int speed;

    private CCColor color;

    private final ArrayList<IStampUpdateListener> changeListeners = new ArrayList<>();

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
            alertChangeListeners(IStampUpdateListener.TYPE.INPUT);
        }
    }

    @Override
    public Rectangle render(Graphics g_, InputContainer input, CCVector2Int position, Double[] difference, boolean isSaveRender, boolean isCensor, int historyPoint) {
        int newSize = (int) ((double)size * difference[0]);
        Graphics2D g = (Graphics2D) g_;

        Paint oldColor = g.getColor();
        Rectangle bounds = g.getClipBounds();
        if(bounds == null && scEditorWindow != null)
            bounds = new Rectangle(0, 0, scEditorWindow.getImage().getWidth(), scEditorWindow.getImage().getHeight());

        Paint paint = new CCColor(color, 255).getGradientPaint(bounds.width, bounds.height);
        g.setPaint(paint);
        g.fillOval(position.getX() - newSize / 2, position.getY() - newSize / 2, newSize, newSize);

        if(scEditorWindow != null && input != null && !input.isKeyPressed(scEditorWindow.getMovementKey())) {
            CCVector2Int p0 = scEditorWindow.getPointOnImage(input.getMousePathPoint(0));
            CCVector2Int p1 = scEditorWindow.getPointOnImage(input.getMousePathPoint(1));

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
        return "editorStampSimpleBrush";
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
        return StampType.SIMPLE_BRUSH;
    }

    @Override
    public void addChangeListener(IStampUpdateListener listener) {
        changeListeners.add(listener);
    }

    public void alertChangeListeners(IStampUpdateListener.TYPE type) {
        for(IStampUpdateListener listener : changeListeners) {
            listener.updated(type);
        }
    }

    @Override
    public boolean doAlwaysRender() {
        return false;
    }
}
