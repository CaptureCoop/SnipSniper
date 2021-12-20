package org.snipsniper.utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DrawUtils {

    public enum DIRECTION {VERTICAL, HORIZONTAL}

    public static BufferedImage createHSVHueBar(int width, int height, DIRECTION direction) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        float hue = 0F;
        float stepHeight = 1F / height;
        float stepWidth = 1F / width;

        if(direction == DIRECTION.VERTICAL) {
            for(int y = 0; y < height; y++) {
                g.setColor(new Color(Color.HSBtoRGB(hue, 1F, 1F)));
                g.drawLine(0, y, width, y);
                hue += stepHeight;
            }
        } else {
            for(int x = 0; x < width; x++) {
                g.setColor(new Color(Color.HSBtoRGB(hue, 1F, 1F)));
                g.drawLine(x, 0, x, height);
                hue += stepWidth;
            }
        }

        g.dispose();
        return image;
    }

    public static BufferedImage createHSVBox(int width, int height, float hue) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        float brightness = 1F;
        float saturation;
        float stepHeight = 1F / height;
        float stepWidth = 1F / width;

        for(int bY = 0; bY < height; bY++) {
            saturation = 0;
            for(int bX = 0; bX < width; bX++) {
                saturation += stepWidth;
                g.setColor(new Color(Color.HSBtoRGB(hue, saturation, brightness)));
                g.drawLine(bX, bY, bX, bY);
            }
            brightness -= stepHeight;
        }

        g.dispose();
        return image;
    }

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
