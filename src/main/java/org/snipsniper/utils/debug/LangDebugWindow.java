package org.snipsniper.utils.debug;

import org.snipsniper.ImageManager;

import javax.swing.*;
import java.awt.*;

public class LangDebugWindow extends JFrame {

    public LangDebugWindow() {
        setTitle("Debug Language Window");
        setSize(512, 512);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(ImageManager.getImage("icons/config.png"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        setLocation(width / 2 - getWidth() / 2, height / 2 - getHeight() / 2);
        add(new JLabel("Coming soon", JLabel.CENTER));
        setVisible(true);
    }

}
