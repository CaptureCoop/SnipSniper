package org.snipsniper.utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class DrawUtils {

    public enum DIRECTION {VERTICAL, HORIZONTAL}

    public static BufferedImage createAlphaBar(Color color, int width, int height, DIRECTION direction) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        int amount = width;
        float step = 255F / width;
        float alpha = 0;

        if(direction == DIRECTION.VERTICAL) {
            amount = height;
            step = 255F / height;
        }

        for(int pos = 0; pos < amount; pos++) {
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)alpha));
            switch(direction) {
                case VERTICAL: g.drawLine(0, pos, width, pos); break;
                case HORIZONTAL: g.drawLine(pos, 0, pos, height); break;
            }
            alpha += step;
        }

        g.dispose();
        return image;
    }

    public static BufferedImage createHSVHueBar(int width, int height, DIRECTION direction) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) image.getGraphics();

        float hue = 0F;
        int amount = width;
        float step = 1F / width;

        if(direction == DIRECTION.VERTICAL) {
            amount = height;
            step = 1F / height;
        }

        for(int pos = 0; pos < amount; pos++) {
            g.setColor(new HSB(hue, 1F, 1F).toRGB());
            switch(direction) {
                case VERTICAL: g.drawLine(0, pos, width, pos); break;
                case HORIZONTAL: g.drawLine(pos, 0, pos, height); break;
            }
            hue += step;
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

        for(int y = 0; y < height; y++) {
            saturation = 0;
            for(int x = 0; x < width; x++) {
                saturation += stepWidth;
                g.setColor(new HSB(hue, saturation, brightness).toRGB());
                g.drawLine(x, y, x, y);
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
