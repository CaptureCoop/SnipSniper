package org.snipsniper.configwindow.folderpreview;

import org.snipsniper.utils.Icons;
import org.snipsniper.utils.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class FolderPreviewRenderer extends JPanel {
    private final FolderPreview folderPreview;
    private final BufferedImage folderIcon = Icons.getImage("icons/folder.png");

    public FolderPreviewRenderer(FolderPreview folderPreview, int minWidth, int minHeight) {
        this.folderPreview = folderPreview;
        setPreferredSize(new Dimension(minWidth, minHeight));
        setMinimumSize(new Dimension(minWidth, minHeight));
    }

    public void refresh() {
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        String content = folderPreview.getText().replaceAll("\\\\", "/");
        content = StringUtils.formatDateArguments(content);
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
