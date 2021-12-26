package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Float;

import javax.swing.*;
import java.awt.*;

public class SSColorChooserPanel extends JPanel {
    private SSColor color;
    private Vector2Float position;

    public SSColorChooserPanel(SSColor color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getPrimaryColor().getRed(), color.getPrimaryColor().getBlue(), color.getPrimaryColor().getGreen(), hsv);
        g.drawImage(DrawUtils.createHSVBox(getWidth(), getHeight(), hsv[2]), 0, 0, this);
    }
}
