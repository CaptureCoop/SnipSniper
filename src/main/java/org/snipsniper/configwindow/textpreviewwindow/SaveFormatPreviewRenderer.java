package org.snipsniper.configwindow.textpreviewwindow;

import org.snipsniper.utils.DrawUtils;
import org.snipsniper.utils.Utils;

import javax.swing.*;
import java.awt.*;

public class SaveFormatPreviewRenderer extends JPanel {
    private TextPreviewWindow textPreviewWindow;

    public static String DEFAULT_FORMAT = "%year%-%month%-%day%__%hour%_%minute%_%second%";

    public SaveFormatPreviewRenderer(int minWidth, int minHeight) {
        Dimension min = new Dimension(minWidth, minHeight);
        setPreferredSize(min);
        setMinimumSize(min);
    }

    public void setTextPreviewWindow(TextPreviewWindow window) {
        textPreviewWindow = window;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.BLACK);
        String raw = textPreviewWindow.getText();
        if(raw.isEmpty())
            raw = DEFAULT_FORMAT;
        String text = Utils.constructFilename(raw, "");
        int margin = 100;
        DrawUtils.drawCenteredString(g, text, new Rectangle(0, 0, getWidth(), getHeight()), new Font("Arial", Font.BOLD, DrawUtils.pickOptimalFontSize((Graphics2D) g, text, getWidth() - margin, getHeight())));
    }

}
