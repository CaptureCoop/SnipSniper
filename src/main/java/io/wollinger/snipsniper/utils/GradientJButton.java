package io.wollinger.snipsniper.utils;

import javax.swing.*;
import java.awt.*;

public class GradientJButton extends JButton {
    private SSColor color;
    private String title;

    public GradientJButton(String title, SSColor color) {
        super(title);
        this.color = color;
        this.title = title;
        setContentAreaFilled(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(color.getGradientPaint(getWidth(), getHeight()));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
        g2.setFont(new Font("TimesRoman", Font.PLAIN, getHeight()));
        if(color.isValidGradient())
            g2.setPaint(new SSColor(Utils.getContrastColor(color.getPrimaryColor()), Utils.getContrastColor(color.getSecondaryColor())).getGradientPaint(getWidth(), getHeight()));
        else
            g2.setColor(Utils.getContrastColor(color.getPrimaryColor()));
        g2.drawString(title, getWidth()/2 - g2.getFontMetrics().stringWidth(title)/2, getHeight()-getHeight()/4);
        g2.dispose();
        super.paint(g2);
    }
}
