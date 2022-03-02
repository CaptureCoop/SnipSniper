package net.snipsniper.utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class DrawUtils {
    public static void drawRect(Graphics g, Rectangle rect) {
        if(rect != null)
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static void fillRect(Graphics g, Rectangle rect) {
        if(rect != null)
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static void drawRect(Graphics2D g, Rectangle rect) {
        if(rect != null)
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static void fillRect(Graphics2D g, Rectangle rect) {
        if(rect != null)
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    public static int pickOptimalFontSize(Graphics2D g, String title, int width, int height) {
        Rectangle2D rect;

        int fontSize = height; //initial value
        do {
            fontSize--;
            Font font = new Font("Arial", Font.PLAIN, fontSize);
            rect = getStringBoundsRectangle2D(g, title, font);
        } while (rect.getWidth() >= width || rect.getHeight() >= height);

        return fontSize;
    }

    public static Rectangle2D getStringBoundsRectangle2D(Graphics g, String title, Font font) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        return fm.getStringBounds(title, g);
    }

    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font ) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
