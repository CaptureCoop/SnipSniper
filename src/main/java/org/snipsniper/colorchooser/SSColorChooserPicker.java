package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.HSB;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Float;

import javax.swing.*;
import java.awt.*;

public class SSColorChooserPicker extends JPanel {
    private SSColor color;
    private Vector2Float position;

    private static final int MARGIN = 10;

    public SSColorChooserPicker(SSColor color) {
        this.color = color;
    }

    @Override
    public void paint(Graphics g) {
        int sizeX = getWidth() - MARGIN;
        int sizeY = getHeight() - MARGIN;
        g.drawImage(DrawUtils.createHSVBox(getWidth(), getHeight(), new HSB(color.getPrimaryColor()).getHue()), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.setColor(Color.BLACK);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
    }
}