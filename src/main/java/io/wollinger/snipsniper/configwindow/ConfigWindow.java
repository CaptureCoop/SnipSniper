package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LangManager;

import javax.swing.*;
import java.awt.*;

public class ConfigWindow extends JFrame {
    private Config config;
    private JPanel mainConfigPanel;
    private JPanel editorConfigPanel;
    private JPanel viewerConfigPanel;

    public ConfigWindow(Config config, boolean showMain, boolean showEditor, boolean showViewer) {
        this.config = config;

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //TODO: Mark settings as closed so that settings can only be opened once.
        setup(showMain, showEditor, showViewer);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    //TODO: Make sure that only pages related to from where we opened the config window are enabled.
    public void setup(boolean showMain, boolean showEditor, boolean showViewer) {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        final int iconSize = 16;
        int index = 0;

        if(showMain) {
            mainConfigPanel = new JPanel();
            setupMainPane();
            tabPane.addTab("General Settings", mainConfigPanel);
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize, iconSize, 0)));
            index++;
        }

        if(showEditor) {
            editorConfigPanel = new JPanel();
            setupEditorPane();
            tabPane.addTab("Editor Settings", editorConfigPanel);
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_editor.getScaledInstance(iconSize,iconSize,0)));
            index++;
        }

        if(showViewer) {
            viewerConfigPanel = new JPanel();
            setupViewerPane();
            tabPane.addTab("Viewer Settings", viewerConfigPanel);
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_viewer.getScaledInstance(iconSize,iconSize,0)));
        }

        add(tabPane);
    }

    public void setupMainPane() {

    }

    public void setupEditorPane() {

    }

    public void setupViewerPane() {

    }

}
