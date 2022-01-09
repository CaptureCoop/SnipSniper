package net.snipsniper.systray;

import net.snipsniper.utils.IFunction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class PopupMenuButton extends JMenuItem {
    private IFunction onClick;
    private boolean isMenuChild = false;

    public PopupMenuButton(String title, BufferedImage icon, JFrame popup, IFunction function, ArrayList<PopupMenu> closeWhenClicked) {
        setText(title);
        setIcon(getPopupIcon(icon));
        onClick = function;
        addActionListener(e -> {
            popup.setVisible(false);
            if(closeWhenClicked != null)
                for(PopupMenu menu : closeWhenClicked)
                    menu.setPopupMenuVisible(false);
            if(onClick != null)
                onClick.run();
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                setArmed(true);
                if(!isMenuChild)
                    for(PopupMenu menu : closeWhenClicked)
                        menu.setPopupMenuVisible(false);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mouseExited(e);
                setArmed(false);
            }
        });
    }

    public void setFunction(IFunction function) {
        onClick = function;
    }

    public void setIsMenuChild(boolean value) {
        isMenuChild = value;
    }

    public ImageIcon getPopupIcon(BufferedImage image) {
        return new ImageIcon(image.getScaledInstance(16,16, Image.SCALE_DEFAULT));
    }

}
