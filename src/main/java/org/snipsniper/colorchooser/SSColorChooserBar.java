package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class SSColorChooserBar extends JPanel {
    private SSColor color;
    private float position;
    private DrawUtils.DIRECTION direction;

    private final static int MARGIN = 10;

    public SSColorChooserBar(SSColor color, DrawUtils.DIRECTION direction) {
        this.color = color;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics g) {
        float[] hsv = new float[3];
        Color.RGBtoHSB(color.getPrimaryColor().getRed(), color.getPrimaryColor().getBlue(), color.getPrimaryColor().getGreen(), hsv);
        int sizeX = getWidth() - MARGIN;
        int sizeY = getHeight() - MARGIN;
        g.drawImage(DrawUtils.createHSVHueBar(sizeX, sizeY, direction), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.setColor(Color.BLACK);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
    }

}
