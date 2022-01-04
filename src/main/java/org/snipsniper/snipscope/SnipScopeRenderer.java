package org.snipsniper.snipscope;

import net.capturecoop.ccutils.math.Vector2Int;
import org.snipsniper.snipscope.ui.SnipScopeUIComponent;
import org.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeRenderer extends JPanel {
    private final SnipScopeWindow snipScopeWindow;
    private final RenderingHints qualityHints = Utils.getRenderingHints();
    private final double zoomAntialisingKickIn = 2D;

    public Rectangle lastRectangle;

    public SnipScopeRenderer(SnipScopeWindow snipScopeWindow) {
        this.snipScopeWindow = snipScopeWindow;
    }

    public void paint(Graphics _g) {
        Graphics2D g = (Graphics2D) _g;

        if(snipScopeWindow.getZoom() < zoomAntialisingKickIn)
            g.setRenderingHints(qualityHints);

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
            g.drawImage(image, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
            g.setColor(Color.BLACK);
            //TODO: add config option for outline
            g.drawRect(lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height);
        }
    }

    public void renderUI(Graphics2D g) {
        for(SnipScopeUIComponent component : snipScopeWindow.getUiComponents())
            component.render(g);
    }

}
