package org.snipsniper.configwindow.iconwindow;

import org.snipsniper.utils.Function;
import org.snipsniper.utils.IDJButton;
import org.snipsniper.utils.Icons;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class IconWindow extends JFrame {
    private IconWindow instance;
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<JButton> buttonList = new ArrayList<>();

    public IconWindow(Function onSelectIcon) {
        instance = this;
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
        gbc.gridx = 0;
        final int MAX_X = 4;
        String[] list = Icons.getListAsString();
        int size = getRootPane().getWidth()/5;
        for(int i = 0; i < list.length; i++) {
            if(list[i].contains("icons")) {
                IDJButton button = new IDJButton(list[i]);
                button.addActionListener(actionEvent -> {
                    onSelectIcon.run(button.getID());
                    dispose();
                });
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
        JButton customButton = new JButton("Custom");
        customButton.setPreferredSize(new Dimension(size, size));
        customButton.setMinimumSize(new Dimension(size, size));
        customButton.setMaximumSize(new Dimension(size, size));
        customButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image", "png"));
                int option = fileChooser.showOpenDialog(instance);
                if(option == JFileChooser.APPROVE_OPTION) {
                    //TODO: Copy image
                    onSelectIcon.run("custom");
                    dispose();
                }
            }
        });
        content.add(customButton, gbc);
        pack();
        setSize(getWidth(), 256);
    }
}
