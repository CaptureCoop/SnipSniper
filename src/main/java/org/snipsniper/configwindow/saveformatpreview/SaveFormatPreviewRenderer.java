package org.snipsniper.configwindow.saveformatpreview;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.Utils;

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
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        String text = Utils.constructFilename(saveFormatPreview.getText(), "");
        DrawUtils.drawCenteredString(g, text, new Rectangle(0, 0, getWidth(), getHeight()), new Font("Arial", Font.BOLD, getHeight()/4));
    }

}
