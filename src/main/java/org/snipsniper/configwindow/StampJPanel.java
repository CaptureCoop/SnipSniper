package org.snipsniper.configwindow;

import org.snipsniper.ImageManager;
import org.snipsniper.sceditor.stamps.IStamp;
import org.snipsniper.utils.Utils;
import org.snipsniper.utils.Vector2Int;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class StampJPanel extends JPanel {
    private IStamp stamp;
    private BufferedImage background;
    private boolean backgroundEnabled = true;
    private RenderingHints qualityHints;

    private int margin = 0;

    public void setStamp(IStamp stamp) {
        this.stamp = stamp;
        repaint();
    }

    public void setMargin(int margin) {
        this.margin = margin;
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

        int sizeX = getWidth() - margin;
        int sizeY = getHeight() - margin;

        Graphics2D g2d = (Graphics2D) g;

        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        if(qualityHints == null) {
            qualityHints = Utils.getRenderingHints();
        }

        g2d.setRenderingHints(qualityHints);

        if(background != null && backgroundEnabled) {
            if(!(background.getWidth() >= getWidth() && background.getHeight() >= getHeight()))
                background = ImageManager.getCodePreview();
            int pos = margin / 2;
            int width = sizeX + margin / 2;
            int height = sizeY + margin / 2;
            g2d.drawImage(background, pos, pos, width, height, pos, pos, width, height, null);
            Color oldColor = g2d.getColor();
            g2d.setColor(Color.BLACK);
            g2d.drawRect(pos, pos, width - margin / 2, height - margin / 2);
            g2d.setColor(oldColor);
        }
        if(!isEnabled())
            g2d.setColor(Utils.getDisabledColor());
        g2d.drawRect(0,0,getWidth() - 1,getHeight() -1);

        if(stamp != null)
            stamp.render(g2d, null, new Vector2Int(getWidth()/2, getHeight()/2), new Double[]{1D, 1D}, false, false, 0);

        if(!isEnabled())
            g2d.fillRect(0, 0, getWidth(), getWidth()/2);
        g2d.dispose();
    }

}
