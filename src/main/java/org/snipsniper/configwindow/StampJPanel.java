package org.snipsniper.configwindow;

import org.snipsniper.sceditor.stamps.IStamp;
import org.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StampJPanel extends JPanel {
    private IStamp stamp;
    private BufferedImage background;
    private boolean backgroundEnabled = true;
    private RenderingHints qualityHints;

    public void setStamp(IStamp stamp) {
        this.stamp = stamp;
        repaint();
    }

    public void setBackground(BufferedImage image) {
        background = image;
    }

    public void setBackgroundEnabled(boolean enabled) {
        backgroundEnabled = enabled;
        repaint();
    }

    public void paint(Graphics g) {
        super.paint(g);

        Graphics2D g2d = (Graphics2D) g;

        if(qualityHints == null) {
            qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        }

        g2d.setRenderingHints(qualityHints);

        if(background != null && backgroundEnabled) {
            if(background.getWidth() >= getWidth() && background.getHeight() >= getHeight()) {
                g2d.drawImage(background, 0, 0, getWidth(), getWidth()/2, 0, 0, getWidth(), getWidth()/2, null);
            }
        }

        g2d.drawRect(0,0,getWidth()-1,getWidth()/2-1);

        if(stamp != null)
            stamp.render(g2d, null, new Vector2Int(getWidth()/2, getWidth()/4), new Double[]{1D, 1D}, false, false, 0);
        g2d.dispose();
    }

}
