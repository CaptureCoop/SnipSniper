package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class ColorChooserGradient extends JPanel {
    private SSColor color;

    public void setColor(SSColor color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g) {
        if(color != null) {
            Graphics2D g2d = (Graphics2D) g;
            int offset = 20;
            int size = getHeight()-offset;

            if(color.isValidGradient()) g2d.setPaint(color.getGradientPaint(size, size));
            else g2d.setColor(color.getPrimaryColor());

            g2d.fillRect(getWidth() / 2 - size / 2, offset/2, size, size);

            g2d.dispose();
        }
    }
}
