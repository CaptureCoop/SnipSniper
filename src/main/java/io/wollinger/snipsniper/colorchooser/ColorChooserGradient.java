package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.utils.SSColor;
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

    public ColorChooserGradient() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                if(point1Rect != null && point1Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 0;
                } else if(point2Rect != null && point2Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 1;
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

    public void setColor(SSColor color) {
        this.color = color;
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

            if(color.isValidGradient()) g2d.setPaint(color.getGradientPaint(size, size));
            else g2d.setColor(color.getPrimaryColor());

            g2d.fillRect(startX, startY, size, size);

            point1Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint1().getX()), (int) (lastSize * color.getPoint1().getY()), offset, offset);
            point2Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint2().getX()), (int) (lastSize * color.getPoint2().getY()), offset, offset);

            g2d.setColor(Color.GREEN);
            g2d.fillRect((int) point1Rect.getX(), (int) point1Rect.getY(), (int) point1Rect.getWidth(), (int) point1Rect.getHeight());
            g2d.fillRect((int) point2Rect.getX(), (int) point2Rect.getY(), (int) point2Rect.getWidth(), (int) point2Rect.getHeight());

            g2d.dispose();
        }
    }
}
