package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class SSColorChooserAlphaBar extends JPanel {
    private SSColor color;
    private float position;
    private DrawUtils.DIRECTION direction;
    private final static int MARGIN = 10;

    public SSColorChooserAlphaBar(SSColor color, DrawUtils.DIRECTION direction) {
        this.color = color;
        this.direction = direction;
    }

    @Override
    public void paint(Graphics g) {
        int sizeX = getWidth() - MARGIN;
        int sizeY = getHeight() - MARGIN;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(DrawUtils.createAlphaBar(sizeX, sizeY, direction), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
    }
}
