package io.wollinger.snipsniper.colorchooser;

import javax.swing.*;
import java.awt.*;

public class ColorChooserSingle extends JPanel {
    private Color color;

    public ColorChooserSingle(ColorChooserPreviewPanel previewPanel) {

    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g) {
        if(color != null) {
            g.setColor(color);
            g.fillRect(getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight());
        }
    }
}
