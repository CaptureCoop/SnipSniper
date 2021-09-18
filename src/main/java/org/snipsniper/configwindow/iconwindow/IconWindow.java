package org.snipsniper.configwindow.iconwindow;

import org.snipsniper.utils.Function;
import org.snipsniper.utils.IClosable;
import org.snipsniper.utils.IDJButton;
import org.snipsniper.utils.Icons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;

public class IconWindow extends JFrame implements IClosable {
    private final IconWindow instance;

    public IconWindow(String title, JFrame parent, Function onSelectIcon) {
        instance = this;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(512, 256);
        setTitle(title);
        setIconImage(Icons.getImage("icons/folder.png"));
        setLocation((int)parent.getLocation().getX() + parent.getWidth() / 2 - getWidth() / 2, (int)parent.getLocation().getY() + parent.getHeight() / 2 - getHeight() / 2);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                close();
            }
        });
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
        gbc.gridx = 0;
        final int MAX_X = 4;
        String[] list = Icons.getListAsString();
        int size = getRootPane().getWidth()/5;
        JButton defaultButton = new JButton("Default");
        defaultButton.addActionListener(e -> {
            onSelectIcon.run("none");
            dispose();
        });
        content.add(defaultButton, gbc);
        gbc.gridx++;
        for (String file : list) {
            if (file.contains("icons")) {
                IDJButton button = new IDJButton(file);
                button.addActionListener(actionEvent -> {
                    onSelectIcon.run(button.getID());
                    dispose();
                });
                if (file.endsWith(".png"))
                    button.setIcon(new ImageIcon(Icons.getImage(file).getScaledInstance(size, size, 0)));
                else if (file.endsWith(".gif"))
                    button.setIcon(new ImageIcon(Icons.getAnimatedImage(file).getScaledInstance(size, size, 0)));
                content.add(button, gbc);
                gbc.gridx++;
                if (gbc.gridx >= MAX_X)
                    gbc.gridx = 0;
            }
        }
        JButton customButton = new JButton("Custom");
        customButton.setPreferredSize(new Dimension(size, size));
        customButton.setMinimumSize(new Dimension(size, size));
        customButton.setMaximumSize(new Dimension(size, size));
        customButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image", "png"));
            int option = fileChooser.showOpenDialog(instance);
            if(option == JFileChooser.APPROVE_OPTION) {
                onSelectIcon.run("custom", fileChooser.getSelectedFile().getAbsolutePath());
                dispose();
            }
        });
        content.add(customButton, gbc);
        pack();
        setSize(getWidth(), 256);
    }

    @Override
    public void close() {
        dispose();
    }
}
