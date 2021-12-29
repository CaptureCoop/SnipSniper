package org.snipsniper.sceditor.ezmode;

import org.snipsniper.sceditor.SCEditorWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;

public class EzModeStampTab extends JPanel {
    private final BufferedImage image;

    public EzModeStampTab(BufferedImage image, int size, SCEditorWindow scEditorWindow, int stampIndex) {
        this.image = image;
        setPreferredSize(new Dimension(size, size));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                scEditorWindow.setSelectedStamp(stampIndex);
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                scEditorWindow.setSelectedStamp(stampIndex);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

}
