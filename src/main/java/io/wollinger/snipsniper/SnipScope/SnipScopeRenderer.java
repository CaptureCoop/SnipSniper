package io.wollinger.snipsniper.SnipScope;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeRenderer extends JPanel {
    private SnipScopeWindow snipScopeWindow;

    public SnipScopeRenderer(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
    }

    public void paint(Graphics g) {
        Dimension optimalDimension = snipScopeWindow.getOptimalImageDimension();
        BufferedImage image = snipScopeWindow.getImage();
        if(image != null && optimalDimension != null) {
            int x = getWidth()/2 - (int)(optimalDimension.getWidth()/2);
            int y = getHeight()/2 - (int)(optimalDimension.getHeight()/2);
            g.drawImage(image, x, y, (int)optimalDimension.getWidth(), (int)optimalDimension.getHeight(), this);
        }
    }

}
