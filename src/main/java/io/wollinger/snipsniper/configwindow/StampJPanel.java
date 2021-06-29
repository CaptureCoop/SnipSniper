package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.sceditor.stamps.IStamp;
import io.wollinger.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StampJPanel extends JPanel {
    private IStamp stamp;
    private BufferedImage background;
    private boolean backgroundEnabled = true;

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

        if(background != null && backgroundEnabled) {
            if(background.getWidth() >= getWidth() && background.getHeight() >= getHeight()) {
                g.drawImage(background, 0, 0, getWidth(), getHeight(), 0, 0, getWidth(), getHeight(), null);
            }
        }

        g.drawRect(0,0,getWidth()-1,getHeight()-1);

        if(stamp != null)
            stamp.render(g, null, new Vector2Int(getWidth()/2, getHeight()/2), new Double[]{1D, 1D}, false, false, 0);
    }

}
