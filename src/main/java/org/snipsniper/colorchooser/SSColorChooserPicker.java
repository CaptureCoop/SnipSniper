package org.snipsniper.colorchooser;

import org.snipsniper.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SSColorChooserPicker extends JPanel {
    private SSColor color;
    private Vector2Float position;

    private static final int MARGIN = 10;

    private boolean isDragging = false;

    public SSColorChooserPicker(SSColor color) {
        this.color = color;
        updatePosition();
        color.addChangeListener(changeEvent -> updatePosition());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                Rectangle rect = getSelectRect();
                if(rect == null)
                    return;

                if(rect.contains(mouseEvent.getPoint()))
                    isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                isDragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if(isDragging) {
                    float percentageX = (mouseEvent.getX() * 100F) / getWidth();
                    float percentageY = (mouseEvent.getY() * 100F) / getHeight();
                    float pointX = new Vector2Float(percentageX / 100F, 0).limitX(0.01F, 0.99F).getX();
                    float pointY = new Vector2Float(percentageY / 100F, 0).limitX(0.01F, 0.99F).getX();
                    HSB current = new HSB(color.getPrimaryColor());
                    color.setPrimaryColor(new HSB(current.getHue(), position.getX(), position.getY(), current.getAlpha()).toRGB());
                    pointY = (pointY - 1) * - 1;
                    position = new Vector2Float(pointX, pointY);
                }
                repaint();
            }
        });
    }

    public Rectangle getSelectRect() {
        int posX = (int)(position.getX() * getSizeX());
        int posY = (int)((position.getY() - 1) * - 1 * getSizeY());
        return new Rectangle(posX, posY, MARGIN, MARGIN);
    }

    public void updatePosition() {
        if(!isDragging) {
            HSB hsb = new HSB(color.getPrimaryColor());
            position = new Vector2Float(hsb.getSaturation(), hsb.getBrightness());
        }
    }

    public int getSizeX() {
        return getWidth() - MARGIN;
    }

    public int getSizeY() {
        return getHeight() - MARGIN;
    }

    @Override
    public void paint(Graphics g) {
        int sizeX = getSizeX();
        int sizeY = getSizeY();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(DrawUtils.createHSVBox(getWidth(), getHeight(), new HSB(color.getPrimaryColor()).getHue()), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.setColor(Color.BLACK);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
        g.setColor(Color.PINK);
        Rectangle rect = getSelectRect();
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }
}
