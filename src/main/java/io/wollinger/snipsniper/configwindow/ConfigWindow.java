package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LangManager;

import javax.swing.*;
import java.awt.*;

public class ConfigWindow extends JFrame {
    private Config config;

    public ConfigWindow(Config config, boolean showMain, boolean showEditor, boolean showViewer) {
        this.config = config;

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //TODO: Mark settings as closed so that settings can only be opened once.
        setup();
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    //TODO: Make sure that only pages related to from where we opened the config window are enabled.
    public void setup() {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        JPanel mainConfigPanel = new JPanel();
        JPanel editorConfigPanel = new JPanel();
        JPanel viewerConfigPanel = new JPanel();

        tabPane.addTab("General Settings", mainConfigPanel);
        tabPane.addTab("Editor Settings", editorConfigPanel);
        tabPane.addTab("Viewer Settings", viewerConfigPanel);
        final int iconSize = 16;
        tabPane.setIconAt(0, new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize,iconSize,0)));
        tabPane.setIconAt(1, new ImageIcon(Icons.icon_editor.getScaledInstance(iconSize,iconSize,0)));
        tabPane.setIconAt(2, new ImageIcon(Icons.icon_viewer.getScaledInstance(iconSize,iconSize,0)));

        add(tabPane);
    }

}
