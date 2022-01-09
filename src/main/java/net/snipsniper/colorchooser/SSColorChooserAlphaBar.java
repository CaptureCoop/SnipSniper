package net.snipsniper.colorchooser;

import net.capturecoop.ccutils.math.MathUtils;
import net.capturecoop.ccutils.math.Vector2Float;
import net.snipsniper.ImageManager;
import net.snipsniper.utils.DrawUtils;
import net.snipsniper.utils.SSColor;
import org.snipsniper.utils.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

public class SSColorChooserAlphaBar extends JPanel {
    private SSColor color;
    private float position;
    private DrawUtils.DIRECTION direction;

    private final static int MARGIN = 10;
    private final static int SEL_MARGIN = 4;
    private final static int SEL_MARGIN_OFF = 2;

    private boolean isDragging = false;

    public SSColorChooserAlphaBar(SSColor color, DrawUtils.DIRECTION direction, boolean alwaysGrab) {
        this.color = color;
        this.direction = direction;
        updateAlpha();
        color.addChangeListener(e -> updateAlpha());
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                Rectangle rect = getSelectRect();
                if(rect == null)
                    return;

                if(rect.contains(mouseEvent.getPoint()) && !alwaysGrab)
                    isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                isDragging = false;
                if(alwaysGrab)
                    execute(mouseEvent.getX(), mouseEvent.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                if(isDragging || alwaysGrab)
                    execute(mouseEvent.getX(), mouseEvent.getY());
            }
        });
    }

    private void execute(int x, int y) {
        int pos = y;
        int size = getHeight();
        if(direction == DrawUtils.DIRECTION.HORIZONTAL) {
            pos = x;
            size = getWidth();
        }
        float percentage = (pos * 100F) / size;
        position = new Vector2Float(percentage / 100F, 0).limitX(0, 1).getX();
        Color oldColor = color.getPrimaryColor();
        color.setPrimaryColor(new Color(oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), MathUtils.clampInt((int)(position * 255), 0, 255)));
        repaint();
    }

    private void updateAlpha() {
        if(!isDragging) {
            position = ((color.getPrimaryColor().getAlpha() * 100F) / 255F) / 100F;
        }
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
        int sizeX = getSizeX();
        int sizeY = getSizeY();
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        int amount = sizeX / sizeY;
        for(int i = 0; i < amount; i++) {
            g.drawImage(ImageManager.getImage("ui/transparent_grid_4x4.png"), MARGIN / 2 + i * sizeY, MARGIN / 2, sizeY, sizeY, this);
        }
        g.drawImage(DrawUtils.createAlphaBar(color.getPrimaryColor(), sizeX, sizeY, direction), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        g.setColor(Color.BLACK);
        g.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
        g.setColor(Color.GRAY);
        Rectangle rect = getSelectRect();
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }
}
