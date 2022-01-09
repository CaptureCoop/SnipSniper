package net.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

public class GradientJButton extends JButton {
    private final SSColor color;
    private final String title;

    public GradientJButton(String title, SSColor color) {
        super(title);
        this.color = color;
        this.title = title;
        setContentAreaFilled(false);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setPaint(color.getGradientPaint(getWidth(), getHeight()));
        if(!isEnabled())
            g2.setPaint(Utils.getDisabledColor());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(Color.BLACK);
        g2.drawRect(0, 0, getWidth()-1, getHeight()-1);
        int drawHeight = (int)(getHeight() / 1.5F);
        g2.setFont(new Font(getFont().getFontName(), Font.PLAIN, drawHeight));

        FontRenderContext frc = g2.getFontRenderContext();
        GlyphVector gv = g2.getFont().createGlyphVector(frc, getText());
        Shape shape = gv.getOutline();

        AffineTransform oldTransform = g2.getTransform();
        g2.translate(getWidth()/2 - g2.getFontMetrics().stringWidth(title)/2, getHeight()/2 + drawHeight / 3);
        g2.setStroke(new BasicStroke(2));
        g2.draw(shape);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.setTransform(oldTransform);

        if(!isEnabled()) {
            g2.setColor(Utils.getDisabledColor());
            g2.fillRect(0, 0, getWidth(), getHeight());
        }

        g2.dispose();
        super.paint(g2);
    }
}
