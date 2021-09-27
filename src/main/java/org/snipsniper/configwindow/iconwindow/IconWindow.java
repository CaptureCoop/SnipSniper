package org.snipsniper.configwindow.iconwindow;

import org.snipsniper.ImageManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.utils.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;


public class IconWindow extends JFrame implements IClosable {
    private final IconWindow instance;
    private final IFunction onSelectIcon;

    public IconWindow(String title, JFrame parent, IFunction onSelectIcon) {
        instance = this;
        this.onSelectIcon = onSelectIcon;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(512, 256);
        setTitle(title);
        setIconImage(ImageManager.getImage("icons/folder.png"));
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
        populateButtons(content);
        pack();
        setSize(getWidth(), 256);
    }

    public void populateButtons(JPanel content) {
        content.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        final int MAX_X = 4;
        ArrayList<SSFile> list = new ArrayList<>();
        for(String file : ImageManager.getListAsString())
            if(file.contains("icons"))
                list.add(new SSFile(file, SSFile.LOCATION.JAR));
        for(File localFile : FileUtils.listFiles(SnipSniper.getMainFolder() + "/img/")) {
            list.add(new SSFile(localFile.getName(), SSFile.LOCATION.LOCAL));
        }
        int size = getRootPane().getWidth()/5;
        JButton defaultButton = new JButton("Default");
        defaultButton.addActionListener(e -> {
            onSelectIcon.run("none");
            dispose();
        });
        content.add(defaultButton, gbc);
        gbc.gridx++;
        for (SSFile file : list) {
            IconButton button = new IconButton(file.getPathWithLocation(), file.getLocation());
            button.setOnSelect(args -> {
                onSelectIcon.run(button.getID());
                dispose();
            });

            button.setOnDelete(args -> populateButtons(content));

            switch(file.getLocation()) {
                case JAR:
                    if(file.getPath().endsWith(".png"))
                        button.setIcon(new ImageIcon(ImageManager.getImage(file.getPath()).getScaledInstance(size, size, 0)));
                    else if(file.getPath().endsWith(".gif"))
                        button.setIcon(new ImageIcon(ImageManager.getAnimatedImage(file.getPath()).getScaledInstance(size, size, 0)));
                    break;
                case LOCAL:
                    button.setIcon(new ImageIcon(Utils.getImageFromDisk(SnipSniper.getImageFolder() + "/" + file.getPath()).getScaledInstance(size, size, 0)));
                    break;
            }

            content.add(button, gbc);
            gbc.gridx++;
            if (gbc.gridx >= MAX_X)
                gbc.gridx = 0;
        }
        JButton customButton = new JButton("Custom");
        customButton.setPreferredSize(new Dimension(size, size));
        customButton.setMinimumSize(new Dimension(size, size));
        customButton.setMaximumSize(new Dimension(size, size));
        customButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileFilter fileFilter = new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if(f.isDirectory())
                        return true;
                    return StringUtils.endsWith(f.getName(), ".png", ".gif", ".jpg", ".jpeg");
                }

                @Override
                public String getDescription() {
                    return "Images";
                }
            };
            fileChooser.addChoosableFileFilter(fileFilter);
            fileChooser.setFileFilter(fileFilter);
            int option = fileChooser.showOpenDialog(instance);
            if(option == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    Files.copy(file.toPath(), new File(SnipSniper.getImageFolder() + "/" + file.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    populateButtons(content);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        content.add(customButton, gbc);
        content.revalidate();
        content.repaint();
    }

    @Override
    public void close() {
        dispose();
    }
}
