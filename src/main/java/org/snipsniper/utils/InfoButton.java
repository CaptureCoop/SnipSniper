package org.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InfoButton extends JButton {
    private final String info;
    private JFrame window;

    public InfoButton(String info) {
        if(info == null) {
            this.info = "No text provided";
        } else {
            this.info = info;
        }
        setText("?");

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                new java.util.Timer().schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        if(isShowing()) {
                            Rectangle rect = new Rectangle((int) getLocationOnScreen().getX(), (int) getLocationOnScreen().getY(), getBounds().width, getBounds().height);
                            if (rect.contains(MouseInfo.getPointerInfo().getLocation())) {
                                openWindow();
                            }
                        }
                    }
                }, 1000);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                closeWindow();
            }
        });
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(new Color(0, 0, 0, 0));
        g.drawRect(0, 0, getWidth(), getHeight());
        int iconSize = 16;
        g.drawImage(Icons.getImage("icons/questionmark.png").getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH), getWidth() /2 - iconSize / 2, getHeight() / 2 - iconSize / 2, iconSize, iconSize, this);
    }

    public void closeWindow() {
        if(window != null) {
            window.dispose();
            window = null;
        }
    }

    public void openWindow() {
        if(window != null) {
            window.requestFocus();
            return;
        }
        window = new JFrame();
        window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        window.setLocation((int) (getLocationOnScreen().getX() + getWidth()), (int) (getLocationOnScreen().getY()));
        window.getContentPane().setBackground(Color.WHITE);
        window.setUndecorated(true);
        JLabel content = new JLabel("<html><p style=\"width:256px;\">" + info + "</p></html>");
        content.setVerticalAlignment(JLabel.TOP);
        window.add(content);
        window.setMinimumSize(new Dimension(256, 128));
        window.getRootPane().setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK));
        window.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    window.dispose();
            }
        });
        window.setVisible(true);
        window.pack();
    }
}