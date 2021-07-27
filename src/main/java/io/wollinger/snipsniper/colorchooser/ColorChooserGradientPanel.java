package io.wollinger.snipsniper.colorchooser;

import javax.swing.*;
import java.awt.*;

public class ColorChooserGradientPanel extends JPanel {
    public ColorChooserGradientPanel(ColorChooser colorChooser) {
        setPreferredSize(new Dimension(colorChooser.getWidth(), 256));
    }

    @Override
    public void paint(Graphics g) {
        //g.fillRect(0,0, getWidth(), getHeight());
    }
}
