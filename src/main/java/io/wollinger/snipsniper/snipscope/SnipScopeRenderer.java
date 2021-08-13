package io.wollinger.snipsniper.snipscope;

import io.wollinger.snipsniper.snipscope.ui.SnipScopeUIComponent;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeRenderer extends JPanel {
    private final SnipScopeWindow snipScopeWindow;

    public SnipScopeRenderer(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
    }

    public Rectangle lastRectangle;

    public void paint(Graphics g) {
        Dimension optimalDimension = snipScopeWindow.getOptimalImageDimension();
        BufferedImage image = snipScopeWindow.getImage();
        if(image != null && optimalDimension != null) {
            int x = getWidth()/2 - (int)(optimalDimension.getWidth()/2);
            int y = getHeight()/2 - (int)(optimalDimension.getHeight()/2);

            x -= snipScopeWindow.getZoomOffset().getX();
            y -= snipScopeWindow.getZoomOffset().getY();

            Vector2Int posModifier = snipScopeWindow.getPosition();
            x -= posModifier.getX();
            y -= posModifier.getY();

            float zoom = snipScopeWindow.getZoom();
            lastRectangle = new Rectangle(x, y, (int)(optimalDimension.getWidth()*zoom), (int)(optimalDimension.getHeight()*zoom));
            g.drawImage(image, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height , this);
        }

        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents())
            component.render((Graphics2D) g);
    }

}
