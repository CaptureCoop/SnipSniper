package io.wollinger.snipsniper.utils;

import java.awt.*;

public class DrawUtils {
    public static void drawRect(Graphics g, Rectangle rect) {
        if(rect != null)
            g.drawRect(rect.x, rect.y, rect.width, rect.height);
    }
}
