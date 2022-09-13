package net.snipsniper.snipscope;

import org.capturecoop.ccutils.math.CCVector2Int;
import net.snipsniper.snipscope.ui.SnipScopeUIComponent;
import net.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SnipScopeRenderer extends JPanel {
    private final SnipScopeWindow snipScopeWindow;
    private final RenderingHints qualityHints = Utils.Companion.getRenderingHints();
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
            int x = getWidth() / 2 - optimalDimension.width / 2;
            int y = getHeight() / 2 - optimalDimension.height / 2;

            x -= snipScopeWindow.getZoomOffset().getX();
            y -= snipScopeWindow.getZoomOffset().getY();

            CCVector2Int posModifier = snipScopeWindow.getPosition();
            x -= posModifier.getX();
            y -= posModifier.getY();

            float zoom = snipScopeWindow.getZoom();
            lastRectangle = new Rectangle(x, y, (int)(optimalDimension.width * zoom), (int)(optimalDimension.height * zoom));
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
