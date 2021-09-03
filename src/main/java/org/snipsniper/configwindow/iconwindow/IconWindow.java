package org.snipsniper.configwindow.iconwindow;

import org.snipsniper.utils.Icons;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class IconWindow extends JFrame {
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<JButton> buttonList = new ArrayList<>();

    public IconWindow() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(512, 256);
        addMouseListener(new MouseAdapter() {
            boolean listenForExit = false;
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                super.mouseEntered(mouseEvent);
                listenForExit = true;
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                super.mouseExited(mouseEvent);
                if(listenForExit) {
                    Rectangle bounds = new Rectangle(0, 0, (int)getBounds().getWidth(), (int)getBounds().getHeight());
                    if(!bounds.contains(mouseEvent.getPoint()))
                        dispose();
                }
            }
        });
        JPanel content = new JPanel(new GridBagLayout());
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane);
        setResizable(false);
        setVisible(true);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        final int MAX_X = 4;
        String[] list = Icons.getListAsString();
        for(int i = 0; i < list.length; i++) {
            if(list[i].contains("icons") || list[i].contains("systray")) {
                int size = getRootPane().getWidth()/5;
                JButton button = new JButton();
                if (list[i].endsWith(".png"))
                    button.setIcon(new ImageIcon(Icons.getImage(list[i]).getScaledInstance(size, size, 0)));
                else if (list[i].endsWith(".gif"))
                    button.setIcon(new ImageIcon(Icons.getAnimatedImage(list[i]).getScaledInstance(size, size, 0)));
                content.add(button, gbc);
                gbc.gridx++;
                if (gbc.gridx >= MAX_X)
                    gbc.gridx = 0;
                buttonList.add(button);
            }
        }
        pack();
        setSize(getWidth(), 256);
    }
}
