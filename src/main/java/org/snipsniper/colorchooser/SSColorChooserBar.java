package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class SSColorChooserBar extends JPanel {
    private SSColor color;
    private float position;
    private DrawUtils.DIRECTION direction;

    public SSColorChooserBar(SSColor color, DrawUtils.DIRECTION direction) {
        this.color = color;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics g) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getPrimaryColor().getRed(), color.getPrimaryColor().getBlue(), color.getPrimaryColor().getGreen(), hsv);
        g.drawImage(DrawUtils.createHSVHueBar(getWidth(), getHeight(), direction), 0, 0, this);
    }

}
