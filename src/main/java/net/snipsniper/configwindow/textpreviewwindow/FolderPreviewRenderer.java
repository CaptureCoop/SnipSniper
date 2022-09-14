package net.snipsniper.configwindow.textpreviewwindow;

import net.snipsniper.ImageManager;
import org.capturecoop.ccutils.utils.CCStringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class FolderPreviewRenderer extends JPanel {
    private TextPreviewWindow textPreviewWindow;
    private final BufferedImage folderIcon = ImageManager.Companion.getImage("icons/folder.png");

    public FolderPreviewRenderer(int minWidth, int minHeight) {
        Dimension min = new Dimension(minWidth, minHeight);
        setPreferredSize(min);
        setMinimumSize(min);
    }

    public void setTextPreviewWindow(TextPreviewWindow textPreviewWindow) {
        this.textPreviewWindow = textPreviewWindow;
    }

    @Override
    public void paint(Graphics g) {
        String content = textPreviewWindow.getText().replaceAll("\\\\", "/");
        content = CCStringUtils.formatDateTimeString(content);
        String[] parts = content.split("/");
        ArrayList<String> partsFinal = new ArrayList<>();
        for(String str : parts) {
            if(!str.isEmpty())
                partsFinal.add(str);
        }
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());

        partsFinal.add(0, "Main folder");
        int size = getWidth() / (partsFinal.size());
        if(size > getHeight())
            size = getHeight() / (partsFinal.size());
        int index = 0;
        for(String folder : partsFinal) {
            g.drawImage(folderIcon, index * size, 0, size, size, null);
            g.setFont(new Font("Consolas", Font.PLAIN, size/10));
            g.setColor(Color.BLACK);
            g.drawString(folder, (size / 2 - g.getFontMetrics().stringWidth(folder) / 2) + (size * index), (int) (size/1.8));
            index++;
        }
    }

}
