package org.snipsniper.configwindow.saveformatpreview;

import javax.swing.*;
import java.awt.*;

public class SaveFormatPreviewRenderer extends JPanel {
    private final SaveFormatPreview saveFormatPreview;

    public SaveFormatPreviewRenderer(SaveFormatPreview saveFormatPreview, int minWidth, int minHeight) {
        this.saveFormatPreview = saveFormatPreview;
        Dimension min = new Dimension(minWidth, minHeight);
        setPreferredSize(min);
        setMinimumSize(min);
    }

    public void refresh() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawString(saveFormatPreview.getText(), 30, 30);
    }

}
