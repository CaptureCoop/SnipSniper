package io.wollinger.snipsniper.SnipScope;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.InputContainer;
import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.PrintStream;

public class SnipScopeWindow extends JFrame {
    private Config config;
    private BufferedImage image;
    private String fileLocation;

    private SnipScopeRenderer renderer;

    private Dimension optimalImageDimension;
    private Vector2Int position = new Vector2Int(0,0);
    private Vector2Int zoomOffset = new Vector2Int(0, 0);

    private float zoom = 1;
    private InputContainer inputContainer = new InputContainer();

    public SnipScopeWindow(Config config, BufferedImage image, String fileLocation) {
        this.config = config;
        this.image = image;
        this.fileLocation = fileLocation;

        SnipScopeListener listener = new SnipScopeListener(this);
        addKeyListener(listener);
        renderer = new SnipScopeRenderer(this);
        renderer.addMouseListener(listener);
        renderer.addMouseMotionListener(listener);
        renderer.addMouseWheelListener(listener);
        add(renderer);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(512,512);
        setVisible(true);
        calculateZoom();
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
        PrintStream stream = System.out;

        Dimension optimalDimension = getOptimalImageDimension();
        double imageX = renderer.getWidth()/2 - optimalDimension.getWidth()/2;
        double imageY = renderer.getHeight()/2 - optimalDimension.getHeight()/2;

        imageX -= zoomOffset.x;
        imageY -= zoomOffset.y;

        imageX -= position.x;
        imageY -= position.y;

        System.out.println("Raw point: " + point);

        Utils.printArgs(stream, "IMAGE X:{0}, Y:{1}", imageX, imageY);

        System.out.println("Optimal: " + optimalDimension);
        System.out.println("IMG Size: " + image.getWidth() + " " + image.getHeight());

        double width = optimalDimension.getWidth() * zoom;
        double height = optimalDimension.getHeight() * zoom;

        double posOnImageX = (point.getX() - imageX) * ((double)image.getWidth()/width);
        double posOnImageY = (point.getY() - imageY) * ((double)image.getHeight()/ height);

        Utils.printArgs(stream, "point - imagex/y x:{0} y{1}", point.getX() - imageX, point.getY() - imageY);

        Utils.printArgs(stream, "w: {0}, h:{1}", (double)image.getWidth()/width, (double)image.getHeight()/height);

        Utils.printArgs(stream, "POS ON IMAGE X:{0}, Y:{1}\n", posOnImageX, posOnImageY);

        Vector2Int vec = new Vector2Int(posOnImageX, posOnImageY);
        return vec;
    }


    public Vector2Int getZoomOffset() {
        return zoomOffset;
    }

    public void setZoomOffset(Vector2Int vec2int) {
        zoomOffset = vec2int;
    }

    public SnipScopeRenderer getRenderer() {
        return renderer;
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
