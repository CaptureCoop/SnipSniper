package org.snipsniper.sceditor.ezmode;

import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.sceditor.stamps.StampUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class EzModeStampTab extends JPanel {
    private final BufferedImage image;

    public EzModeStampTab(BufferedImage image, int size, SCEditorWindow scEditorWindow, int stampIndex) {
        this.image = image;
        //TODO: can we somehow enable the preview title even if not directly on the jlabel? :/
        setPreferredSize(new Dimension(size, size));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                scEditorWindow.setSelectedStamp(stampIndex);
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                scEditorWindow.setEzModeTitle(StampUtils.getStampAsString(stampIndex));
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                scEditorWindow.setEzModeTitle(null);
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }

}
