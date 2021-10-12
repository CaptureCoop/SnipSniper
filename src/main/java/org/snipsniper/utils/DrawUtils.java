package org.snipsniper.utils;

import java.awt.*;

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

    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font ) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }
}
