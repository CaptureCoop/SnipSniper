package net.snipsniper.colorchooser;

import org.capturecoop.ccutils.math.Vector2Float;
import net.snipsniper.utils.DrawUtils;
import net.snipsniper.utils.HSB;
import net.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class SSColorChooserHSBPicker extends JPanel {
    private SSColor color;
    private Vector2Float position;

    private static final int MARGIN = 10;

    private boolean isDragging = false;

    private BufferedImage buffer;
    private boolean dirty = true;

    public SSColorChooserHSBPicker(SSColor color, boolean alwaysGrab) {
        this.color = color;
        updatePosition();
        color.addChangeListener(changeEvent -> updatePosition());
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
        dirty = true;
        float percentageX = (x * 100F) / getWidth();
        float percentageY = (y * 100F) / getHeight();
        float pointX = new Vector2Float(percentageX / 100F, 0).limitX(0F, 1F).getX();
        float pointY = new Vector2Float(percentageY / 100F, 0).limitX(0F, 1F).getX();
        HSB current = new HSB(color.getPrimaryColor());
        color.setPrimaryColor(new HSB(current.getHue(), position.getX(), position.getY(), current.getAlpha()).toRGB());
        pointY = (pointY - 1) * - 1;
        position = new Vector2Float(pointX, pointY);
        repaint();
    }

    public Rectangle getSelectRect() {
        int posX = (int)(position.getX() * getSizeX());
        int posY = (int)((position.getY() - 1) * - 1 * getSizeY());
        return new Rectangle(posX, posY, MARGIN, MARGIN);
    }

    public void updatePosition() {
        if(!isDragging) {
            dirty = true;
            HSB hsb = new HSB(color.getPrimaryColor());
            position = new Vector2Float(hsb.getSaturation(), hsb.getBrightness());
            repaint();
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
        if(!dirty) {
            g.drawImage(buffer, 0, 0, this);
            return;
        }

        if(buffer == null || !(buffer.getWidth() == getWidth() && buffer.getHeight() == getHeight())) {
            buffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        Graphics bufferGraphics = buffer.getGraphics();
        dirty = false;

        int sizeX = getSizeX();
        int sizeY = getSizeY();
        bufferGraphics.setColor(getBackground());
        bufferGraphics.fillRect(0, 0, getWidth(), getHeight());
        bufferGraphics.drawImage(DrawUtils.createHSVBox(getWidth(), getHeight(), new HSB(color.getPrimaryColor()).getHue()), MARGIN / 2, MARGIN / 2, sizeX, sizeY, this);
        bufferGraphics.setColor(Color.BLACK);
        bufferGraphics.drawRect(MARGIN / 2 - 1, MARGIN / 2 - 1, sizeX + 1, sizeY + 1);
        bufferGraphics.setColor(Color.GRAY);
        Rectangle rect = getSelectRect();
        bufferGraphics.fillRect(rect.x, rect.y, rect.width, rect.height);
        bufferGraphics.dispose();
        g.drawImage(buffer, 0, 0, this);
    }
}
