package org.snipsniper.systray;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PopupMenu extends JMenu {

    public PopupMenu(String text, BufferedImage icon) {
        setText(text);
        setIcon(new ImageIcon(icon.getScaledInstance(16,16, Image.SCALE_DEFAULT)));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setArmed(true);
                setPopupMenuVisible(true);
                getPopupMenu().setLocation(getLocationOnScreen().x + getWidth(), getLocationOnScreen().y);
           }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setArmed(false);
            }
        });
    }

    public void add(PopupMenuButton menuItem) {
        super.add(menuItem);
        menuItem.setIsMenuChild(true);
        menuItem.addActionListener(e -> setPopupMenuVisible(false));
    }
}
