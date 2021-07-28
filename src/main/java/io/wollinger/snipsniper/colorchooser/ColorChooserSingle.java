package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.utils.SSColor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ColorChooserSingle extends JPanel {
    private SSColor color;

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
