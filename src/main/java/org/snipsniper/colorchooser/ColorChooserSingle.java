package org.snipsniper.colorchooser;

import org.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class ColorChooserSingle extends JPanel {
    private final SSColor color;

    public ColorChooserSingle(ColorChooser colorChooser) {
        color = colorChooser.getColor();
        color.addChangeListener(e -> repaint());
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(color != null) {
            g.setColor(color.getPrimaryColor());
            g.fillRect(getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight());
        }
    }
}
