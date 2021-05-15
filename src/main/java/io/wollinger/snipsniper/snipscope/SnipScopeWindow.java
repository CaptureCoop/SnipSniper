package io.wollinger.snipsniper.snipscope;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class SnipScopeWindow extends JFrame {
    private BufferedImage image;

    private Dimension optimalImageDimension;
    private Vector2Int position = new Vector2Int(0,0);
    private Vector2Int zoomOffset = new Vector2Int(0, 0);

    private SnipScopeRenderer renderer;

    private float zoom = 1;
    private final InputContainer inputContainer = new InputContainer();

    private int movementKey = KeyEvent.VK_SPACE;
    private boolean requireMovementKeyForZoom = true;

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
            setSize(insets.left + insets.right + image.getWidth(), insets.bottom + insets.top + image.getHeight());
            setOptimalImageDimension(new Dimension(image.getWidth(), image.getHeight()));
        }
    }

    public void setLocationAuto() {
        Dimension dimension = getOptimalImageDimension();
        Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)(screenDimension.getWidth()/2 - dimension.getWidth()/2), (int)(screenDimension.getHeight()/2 - dimension.getHeight()/2));
    }

    public void calculateZoom() {
        int dimWidth = (int) getOptimalImageDimension().getWidth();
        int dimHeight = (int) getOptimalImageDimension().getHeight();

        int offsetX = dimWidth/2;
        int offsetY = dimHeight/2;

        int modX = (int)(offsetX * getZoom() - offsetX);
        int modY = (int)(offsetY * getZoom() - offsetY);
        zoomOffset = new Vector2Int(modX, modY);

        repaint();
    }

    public Double[] getDifferenceFromImage() {
        Dimension optimalDimension = optimalImageDimension;
        double width = (double)image.getWidth() / (optimalDimension.getWidth() * zoom);
        double height = (double)image.getHeight() / (optimalDimension.getHeight() * zoom);
        return new Double[]{width, height};
    }

    public Vector2Int getPointOnImage(Point point) {
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

        return new Vector2Int(posOnImageX, posOnImageY);
    }

    public void resetZoom() {
        setPosition(new Vector2Int());
        setZoom(1);
        setZoomOffset(new Vector2Int());
    }

    public void resizeTrigger() {
        setOptimalImageDimension(Utils.getScaledDimension(image, renderer.getSize()));
        calculateZoom();
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        optimalImageDimension = Utils.getScaledDimension(image, renderer.getSize());
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

    public Vector2Int getZoomOffset() {
        return zoomOffset;
    }

    public void setZoomOffset(Vector2Int vec2int) {
        zoomOffset = vec2int;
    }

    public BufferedImage getImage() {
        return image;
    }

    public Vector2Int getPosition() {
        return position;
    }

    public void setPosition(Vector2Int vec2int) {
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

}
