package io.wollinger.snipsniper.snipscope;

import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeWindow extends JFrame {
    private BufferedImage image;

    private final SnipScopeRenderer renderer;

    private Dimension optimalImageDimension;
    private Vector2Int position = new Vector2Int(0,0);
    private Vector2Int zoomOffset = new Vector2Int(0, 0);

    private float zoom = 1;
    private final InputContainer inputContainer = new InputContainer();

    public SnipScopeWindow(BufferedImage image) {
        this.image = image;

        SnipScopeListener listener = new SnipScopeListener(this);
        addKeyListener(listener);
        renderer = new SnipScopeRenderer(this);
        renderer.addMouseListener(listener);
        renderer.addMouseMotionListener(listener);
        renderer.addMouseWheelListener(listener);
        add(renderer);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

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

        Dimension dimension = getOptimalImageDimension();
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

    public Vector2Int getPointOnImage(Point point) {
        Dimension optimalDimension = getOptimalImageDimension();
        double imageX = (double)renderer.getWidth()/2 - optimalDimension.getWidth()/2;
        double imageY = (double)renderer.getHeight()/2 - optimalDimension.getHeight()/2;

        imageX -= zoomOffset.x;
        imageY -= zoomOffset.y;

        imageX -= position.x;
        imageY -= position.y;

        double width = optimalDimension.getWidth() * zoom;
        double height = optimalDimension.getHeight() * zoom;

        double posOnImageX = (point.getX() - imageX) * ((double)image.getWidth()/width);
        double posOnImageY = (point.getY() - imageY) * ((double)image.getHeight()/ height);

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
