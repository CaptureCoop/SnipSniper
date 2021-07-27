package io.wollinger.snipsniper.colorchooser;

import javax.swing.*;
import java.awt.*;

public class ColorChooserSingle extends JPanel {
    private Color color;

    public ColorChooserSingle(int width, int height) {
        System.out.println(width);
        this.setPreferredSize(new Dimension(width, height));
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g) {
        if(color != null) {
            g.setColor(color);
            g.fillRect(0, 0, getHeight(), getHeight());
        }
    }
}
