package net.snipsniper.colorchooser;

import org.capturecoop.ccutils.math.CCVector2Float;
import net.snipsniper.utils.DrawUtils;
import net.snipsniper.utils.SSColor;
import net.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class ColorChooserGradient extends JPanel {
    private final SSColor color;
    private int lastStartX;
    private int lastStartY;
    private int lastSize;
    private final BufferedImage previewBackground;

    private Rectangle point1Rect;
    private Rectangle point2Rect;

    private int pointControlled = -1; //-1 -> None, 0 -> Point1, 1 -> Point2
    private int lastPointControlled = 0; //As above, however it is not reset upon mouseReleased but set

    BufferedImage previewBuffer;

    public ColorChooserGradient(ColorChooser colorChooser, BufferedImage previewBackground) {
        color = colorChooser.getColor();
        color.addChangeListener(e -> repaint());
        this.previewBackground = previewBackground;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                mousePressedEvent(mouseEvent);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mousePressed(mouseEvent);
                mousePressedEvent(mouseEvent);
            }

            public void mousePressedEvent(MouseEvent mouseEvent) {
                if(point1Rect != null && point1Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 0;
                    lastPointControlled = 0;
                    colorChooser.getJcc().setColor(color.getPrimaryColor());
                } else if(point2Rect != null && point2Rect.contains(mouseEvent.getPoint())) {
                    pointControlled = 1;
                    lastPointControlled = 1;
                    colorChooser.getJcc().setColor(color.getSecondaryColor());
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
                        color.setPoint1(new CCVector2Float(x / size, y / size));
                    else if(pointControlled == 1)
                        color.setPoint2(new CCVector2Float(x / size, y / size));
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
        if(previewBackground != null)
            g.drawImage(previewBackground.getSubimage(0, 0, getHeight(), getHeight()), getWidth() / 2 - getHeight()/2, 0, getHeight(), getHeight(), null);
        if(color != null) {
            Graphics2D g2d = (Graphics2D) g;

            int offset = 20;
            int size = getHeight()-offset;

            int startX = getWidth() / 2 - size / 2;
            int startY = offset/2;

            lastStartX = startX;
            lastStartY = startY;
            lastSize = size;

            if(previewBuffer == null || previewBuffer.getWidth() != size || previewBuffer.getHeight() != size) {
                previewBuffer = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            }

            //TODO: Weird bug when resizing window, smaller to larger destroys gradient preview
            //TODO: Found it! Render the preview as its own BufferedImage that we resize whenever you stop resizing the window :) that will fix it!
            if(color.getSecondaryColor() == null) {
                color.setSecondaryColor(color.getPrimaryColor().brighter());
            }

            Graphics2D previewGraphics = (Graphics2D) previewBuffer.getGraphics();

            Composite oldComposite = previewGraphics.getComposite();
            previewGraphics.setComposite(AlphaComposite.Clear);
            previewGraphics.fillRect(0, 0, size, size);
            previewGraphics.setComposite(oldComposite);

            previewGraphics.setPaint(color.getGradientPaint(size, size));

            previewGraphics.fillRect(0, 0, size, size);

            previewGraphics.dispose();
            g2d.drawImage(previewBuffer, startX, startY, size, size, this);

            point1Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint1().getX()), (int) (lastSize * color.getPoint1().getY()), offset, offset);
            point2Rect = new Rectangle((startX-offset / 2) + (int) (lastSize * color.getPoint2().getX()), (int) (lastSize * color.getPoint2().getY()), offset, offset);

            g2d.setColor(color.getPrimaryColor());
            DrawUtils.fillRect(g2d, point1Rect);
            g2d.setColor(Utils.getContrastColor(color.getPrimaryColor()));
            DrawUtils.drawRect(g2d, point1Rect);
            if(lastPointControlled == 0) {
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(offset/3f));
                DrawUtils.drawRect(g2d, point1Rect);
                g2d.setStroke(oldStroke);
            }

            g2d.setColor(color.getSecondaryColor());
            DrawUtils.fillRect(g2d, point2Rect);
            g2d.setColor(Utils.getContrastColor(color.getSecondaryColor()));
            DrawUtils.drawRect(g2d, point2Rect);
            if(lastPointControlled ==1) {
                Stroke oldStroke = g2d.getStroke();
                g2d.setStroke(new BasicStroke(offset/3f));
                DrawUtils.drawRect(g2d, point2Rect);
                g2d.setStroke(oldStroke);
            }

            g2d.dispose();
        }
    }
}
