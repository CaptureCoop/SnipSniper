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
            g.setColor(color.getPrimaryColor());
            g.fillRect(getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight());
        }
    }
}
