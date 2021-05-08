package io.wollinger.snipsniper.SnipScope;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeWindow extends JFrame {
    private Config config;
    private BufferedImage image;
    private String fileLocation;

    private Dimension optimalImageDimension;
    private Vector2Int position = new Vector2Int(0,0);

    public SnipScopeWindow(Config config, BufferedImage image, String fileLocation) {
        this.config = config;
        this.image = image;
        this.fileLocation = fileLocation;

        SnipScopeListener listener = new SnipScopeListener(this);
        addKeyListener(listener);
        addMouseListener(listener);
        add(new SnipScopeRenderer(this));

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setSize(512,512);
        setVisible(true);
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

    public Dimension getOptimalImageDimension() {
        return optimalImageDimension;
    }

    public void setOptimalImageDimension(Dimension dimension) {
        optimalImageDimension = dimension;
    }

}
