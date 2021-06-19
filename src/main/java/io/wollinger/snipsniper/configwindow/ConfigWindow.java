package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.LangManager;

import javax.swing.*;
import java.awt.*;

public class ConfigWindow extends JFrame {
    private Config config;

    public ConfigWindow(Config config, boolean showMain, boolean showEditor, boolean showViewer) {
        this.config = config;

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

}
