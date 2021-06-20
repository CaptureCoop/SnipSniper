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
            tabPane.addTab("General Settings",  setupMainPane());
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize, iconSize, 0)));
            index++;
        }

        if(showEditor) {
            editorConfigPanel = new JPanel();
            tabPane.addTab("Editor Settings", setupEditorPane());
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_editor.getScaledInstance(iconSize,iconSize,0)));
            index++;
        }

        if(showViewer) {
            viewerConfigPanel = new JPanel();
            tabPane.addTab("Viewer Settings", setupViewerPane());
            tabPane.setIconAt(index, new ImageIcon(Icons.icon_viewer.getScaledInstance(iconSize,iconSize,0)));
        }

        add(tabPane);
    }

    public JScrollPane generateScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    public JComponent setupMainPane() {
        mainConfigPanel.setLayout(new BoxLayout(mainConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            mainConfigPanel.add(new JButton("Main Config Button"));
        }
        return generateScrollPane(mainConfigPanel);
    }

    public JComponent setupEditorPane() {
        editorConfigPanel.setLayout(new BoxLayout(editorConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            editorConfigPanel.add(new JButton("Editor Config Button"));
        }
        return generateScrollPane(editorConfigPanel);
    }

    public JComponent setupViewerPane() {
        viewerConfigPanel.setLayout(new BoxLayout(viewerConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            viewerConfigPanel.add(new JButton("Viewer Config Button"));
        }
        return generateScrollPane(viewerConfigPanel);
    }

}
