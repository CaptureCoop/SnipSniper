package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.utils.DrawUtils;
import io.wollinger.snipsniper.utils.SSColor;
import io.wollinger.snipsniper.utils.Utils;
import io.wollinger.snipsniper.utils.Vector2Float;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class ColorChooserGradient extends JPanel {
    private SSColor color;
    private int lastStartX;
    private int lastStartY;
    private int lastSize;

    private Rectangle point1Rect;
    private Rectangle point2Rect;

    private int pointControlled = -1; //-1 -> None, 0 -> Point1, 1 -> Point2
    private int lastPointControlled = 0; //As above, however it is not reset upon mouseReleased but set

    public ColorChooserGradient(ColorChooser colorChooser) {
        color = colorChooser.getColor();
        color.addChangeListener(e -> repaint());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                if(point1Rect != null && point1Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 0;
                    lastPointControlled = 0;
                } else if(point2Rect != null && point2Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 1;
                    lastPointControlled = 1;
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                super.mouseReleased(mouseEvent);
                pointControlled = -1;
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                super.mouseDragged(mouseEvent);
                if(pointControlled != -1) {
                    float x = mouseEvent.getX() - lastStartX;
                    float y = mouseEvent.getY() - lastStartY;
                    float size = lastSize;
                    if(pointControlled == 0)
                        color.setPoint1(new Vector2Float(x / size, y / size));
                    else if(pointControlled == 1)
                        color.setPoint2(new Vector2Float(x / size, y / size));
                    repaint();
                }
            }
        });
    }

    public void setColorAuto(Color newColor) {
        if(lastPointControlled == 0)
            color.setPrimaryColor(newColor);
        else if(lastPointControlled == 1)
            color.setSecondaryColor(newColor);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(color != null) {
            Graphics2D g2d = (Graphics2D) g;

            int offset = 20;
            int size = getHeight()-offset;

            int startX = getWidth() / 2 - size / 2;
            int startY = offset/2;

            lastStartX = startX;
            lastStartY = startY;
            lastSize = size;

            if(color.getSecondaryColor() == null) {
                color.setSecondaryColor(color.getPrimaryColor().brighter());
            }

            if(color.isValidGradient()) g2d.setPaint(color.getGradientPaint(size, size));
            else g2d.setColor(color.getPrimaryColor());

            g2d.fillRect(startX, startY, size, size);

            point1Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint1().getX()), (int) (lastSize * color.getPoint1().getY()), offset, offset);
            point2Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint2().getX()), (int) (lastSize * color.getPoint2().getY()), offset, offset);

            g2d.setColor(color.getPrimaryColor());
            DrawUtils.fillRect(g2d, point1Rect);
            g2d.setColor(Utils.getContrastColor(color.getPrimaryColor()));
            DrawUtils.drawRect(g2d, point1Rect);
            if(lastPointControlled == 0) {
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(offset/3));
                DrawUtils.drawRect(g2d, point1Rect);
                g2d.setStroke(oldStroke);
            }

            g2d.setColor(color.getSecondaryColor());
            DrawUtils.fillRect(g2d, point2Rect);
            g2d.setColor(Utils.getContrastColor(color.getSecondaryColor()));
            DrawUtils.drawRect(g2d, point2Rect);
            if(lastPointControlled ==1) {
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(offset/3));
                DrawUtils.drawRect(g2d, point2Rect);
                g2d.setStroke(oldStroke);
            }

            g2d.dispose();
        }
    }
}
