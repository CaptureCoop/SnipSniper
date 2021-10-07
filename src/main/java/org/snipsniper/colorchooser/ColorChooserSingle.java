package org.snipsniper.colorchooser;

import org.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorChooserSingle extends JPanel {
    private final SSColor color;
    private final BufferedImage previewBackground;

    public ColorChooserSingle(ColorChooser colorChooser, BufferedImage previewBackground) {
        color = colorChooser.getColor();
        color.addChangeListener(e -> repaint());
        this.previewBackground = previewBackground;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(previewBackground != null)
            g.drawImage(previewBackground.getSubimage(0, 0, getHeight(), getHeight()), getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight(), null);
        if(color != null) {
            g.setColor(color.getPrimaryColor());
            g.fillRect(getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight());
        }
    }
}
