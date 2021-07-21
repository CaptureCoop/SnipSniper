package io.wollinger.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class InfoButton extends JButton {
    private final String info;
    JFrame window;

    public InfoButton(String info) {
        this.info = info;
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
        int size = 16;
        g.setColor(new Color(120, 255, 255, 255));
        g.fillOval(0,0, getWidth()-1, getHeight()-1);
        g.drawImage(Icons.icon_questionmark, getWidth() /2 - size / 2, getHeight() / 2 - size / 2, size, size, this);
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
        window.add(new JLabel(info));
        window.setIconImage(Icons.icon_questionmark);
        window.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) { }

            @Override
            public void keyPressed(KeyEvent keyEvent) { }

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
