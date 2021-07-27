package io.wollinger.snipsniper.systray;

import io.wollinger.snipsniper.utils.Function;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PopupMenuButton extends JMenuItem {
    private Function onClick;

    public PopupMenuButton(String title, BufferedImage icon, JFrame popup, Function function) {
        setText(title);
        setIcon(getPopupIcon(icon));
        onClick = function;
        addActionListener(e -> {
            popup.setVisible(false);
            if(onClick != null)
                onClick.run();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setArmed(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setArmed(false);
            }
        });
    }

    public void setFunction(Function function) {
        onClick = function;
    }

    public ImageIcon getPopupIcon(BufferedImage image) {
        return new ImageIcon(image.getScaledInstance(16,16, Image.SCALE_DEFAULT));
    }

}
