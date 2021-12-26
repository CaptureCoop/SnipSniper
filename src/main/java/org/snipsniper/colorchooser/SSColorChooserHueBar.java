package org.snipsniper.colorchooser;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.HSB;
import org.snipsniper.utils.SSColor;
import org.snipsniper.utils.Vector2Float;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SSColorChooserHueBar extends JPanel {
    private SSColor color;
    private float position;
    private final DrawUtils.DIRECTION direction;

    private final static int MARGIN = 10;
    private final static int SEL_MARGIN = 4;
    private final static int SEL_MARGIN_OFF = 2;
    private boolean hasGrabbed = false;

    public SSColorChooserHueBar(SSColor color, DrawUtils.DIRECTION direction) {
        this.color = color;
        this.direction = direction;
        updateHSV();
        color.addChangeListener(e -> updateHSV());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                Rectangle rect = getSelectRect();
                if(rect == null)
                    return;

                if(rect.contains(mouseEvent.getPoint()))
                    hasGrabbed = true;
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                hasGrabbed = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if(hasGrabbed) {
                    if(direction == DrawUtils.DIRECTION.VERTICAL) {
                        float percentage = (mouseEvent.getY() * 100F) / getHeight();
                        position = new Vector2Float(percentage / 100F, 0).limitX(0F, 0.99F).getX();
                        System.out.println(position);
                        HSB current = new HSB(color.getPrimaryColor());
                        color.setPrimaryColor(new HSB(-position, current.getSaturation(), current.getBrightness()).toRGB());
                    } else if(direction == DrawUtils.DIRECTION.HORIZONTAL) {
                        float percentage = (mouseEvent.getX() * 100F) / getWidth();
                        position = new Vector2Float(percentage / 100F, 0).limitX(0F, 0.99F).getX();
                        HSB current = new HSB(color.getPrimaryColor());
                        color.setPrimaryColor(new HSB(-position, current.getSaturation(), current.getBrightness()).toRGB());
                    }
                }
                repaint();
            }
        });
    }

    private void updateHSV() {
        position = new HSB(color.getPrimaryColor()).getHue();
    }

    private int getSizeX() {
        return getWidth() - MARGIN;
    }

    private int getSizeY() {
        return getHeight() - MARGIN;
    }

    private Rectangle getSelectRect() {
        switch(direction) {
            case VERTICAL:
                int yPos = (int) (getSizeY() / (1 / position)) + MARGIN / 2;
                return new Rectangle(SEL_MARGIN_OFF, yPos - SEL_MARGIN, getWidth() - SEL_MARGIN_OFF * 2, SEL_MARGIN * 2);
            case HORIZONTAL:
                int xPos = (int) (getSizeX() / (1 / position)) + MARGIN / 2;
                return new Rectangle(xPos - SEL_MARGIN, SEL_MARGIN_OFF, SEL_MARGIN * 2, getHeight() - SEL_MARGIN_OFF * 2);
        }
        return null;
    }

    @Override
    public void paint(Graphics g) {
        int sizeX = getWidth() - MARGIN;
        int sizeY = getHeight() - MARGIN;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(DrawUtils.createHSVHueBar(sizeX, sizeY, direction), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.setColor(Color.BLACK);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
        g.setColor(Color.GRAY);
        Rectangle rect = getSelectRect();
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

}
