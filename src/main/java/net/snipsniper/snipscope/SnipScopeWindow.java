package net.snipsniper.snipscope;

import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.snipscope.ui.SnipScopeUIComponent;
import net.snipsniper.utils.InputContainer;
import net.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SnipScopeWindow extends JFrame {
    private BufferedImage image;

    private Dimension optimalImageDimension;
    private CCVector2Int position = new CCVector2Int(0,0);
    private CCVector2Int zoomOffset = new CCVector2Int(0, 0);

    private SnipScopeRenderer renderer;

    private float zoom = 1;
    private final InputContainer inputContainer = new InputContainer();

    private int movementKey = KeyEvent.VK_SPACE;
    private boolean requireMovementKeyForZoom = true;

    private final ArrayList<SnipScopeUIComponent> uiComponents = new ArrayList<>();

    private boolean enableInteraction = true;

    public void init(BufferedImage image, SnipScopeRenderer renderer, SnipScopeListener listener) {
        this.image = image;
        this.renderer = renderer;

        addKeyListener(listener);
        renderer.addMouseListener(listener);
        renderer.addMouseMotionListener(listener);
        renderer.addMouseWheelListener(listener);
        add(renderer);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void setSizeAuto() {
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        if(image.getWidth() >= screenDimension.getWidth() || image.getHeight() > screenDimension.getHeight()) {
            Dimension newDimension = Utils.getScaledDimension(image, screenDimension);
            setOptimalImageDimension(newDimension);
            setSize(newDimension);
        } else {
            Insets insets = getInsets();
            if(insets.top == 0)
                insets.top = getHeight() - renderer.getHeight() - insets.bottom + 1;

            setSize(insets.left + insets.right + image.getWidth(), insets.bottom + insets.top + image.getHeight());
            setLocation(getLocation().x, getLocation().y - insets.top);
            setOptimalImageDimension(new Dimension(image.getWidth(), image.getHeight()));
        }
    }

    public void setLocationAuto() {
        Dimension dimension = getOptimalImageDimension();
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)(screenDimension.getWidth()/2 - dimension.getWidth()/2), (int)(screenDimension.getHeight()/2 - dimension.getHeight()/2));
    }

    public void calculateZoom() {
        int dimWidth = getOptimalImageDimension().width;
        int dimHeight = getOptimalImageDimension().height;

        int offsetX = dimWidth/2;
        int offsetY = dimHeight/2;

        int modX = (int)(offsetX * getZoom() - offsetX);
        int modY = (int)(offsetY * getZoom() - offsetY);
        zoomOffset = new CCVector2Int(modX, modY);

        repaint();
    }

    public Double[] getDifferenceFromImage() {
        Dimension optimalDimension = optimalImageDimension;
        double width = (double)image.getWidth() / (optimalDimension.getWidth() * zoom);
        double height = (double)image.getHeight() / (optimalDimension.getHeight() * zoom);
        return new Double[]{width, height};
    }

    public CCVector2Int getPointOnImage(Point point) {
        if(point == null)
            return null;

        Dimension optimalDimension = optimalImageDimension;
        double imageX = (double)renderer.getWidth()/2 - optimalDimension.getWidth()/2;
        double imageY = (double)renderer.getHeight()/2 - optimalDimension.getHeight()/2;

        imageX -= zoomOffset.getX();
        imageY -= zoomOffset.getY();

        imageX -= position.getX();
        imageY -= position.getY();

        Double[] difference = getDifferenceFromImage();

        double posOnImageX = (point.getX() - imageX) * (difference[0]);
        double posOnImageY = (point.getY() - imageY) * (difference[1]);

        return new CCVector2Int(posOnImageX, posOnImageY);
    }

    public void resetZoom() {
        setPosition(new CCVector2Int());
        setZoom(1);
        setZoomOffset(new CCVector2Int());
    }

    public void resizeTrigger() {
        setOptimalImageDimension(Utils.getScaledDimension(image, renderer.getSize()));
        calculateZoom();
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        optimalImageDimension = Utils.getScaledDimension(image, renderer.getSize());
    }

    public void addUIComponent(SnipScopeUIComponent component) {
        uiComponents.add(component);
    }

    public boolean isPointOnUiComponents(Point point) {
        for(SnipScopeUIComponent component : uiComponents) {
            if(component.contains(point))
                return true;
        }
        return false;
    }

    public ArrayList<SnipScopeUIComponent> getUiComponents() {
        return uiComponents;
    }

    public void setRequireMovementKeyForZoom(boolean value) {
        requireMovementKeyForZoom = value;
    }

    public boolean isRequireMovementKeyForZoom() {
        return requireMovementKeyForZoom;
    }

    public void setMovementKey(int keyCode) {
        movementKey = keyCode;
    }

    public int getMovementKey() {
        return movementKey;
    }

    public CCVector2Int getZoomOffset() {
        return zoomOffset;
    }

    public void setZoomOffset(CCVector2Int vec2int) {
        zoomOffset = vec2int;
    }

    public BufferedImage getImage() {
        return image;
    }

    public CCVector2Int getPosition() {
        return position;
    }

    public void setPosition(CCVector2Int vec2int) {
        position = vec2int;
    }

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public Dimension getOptimalImageDimension() {
        return optimalImageDimension;
    }

    public void setOptimalImageDimension(Dimension dimension) {
        optimalImageDimension = dimension;
    }

    public InputContainer getInputContainer() {
        return inputContainer;
    }

    public void setEnableInteraction(boolean enabled) {
        enableInteraction = enabled;
    }

    public boolean isEnableInteraction() {
        return enableInteraction;
    }
}
