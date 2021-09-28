package org.snipsniper.configwindow.tabs;

import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;

import javax.swing.*;

public class EditorTab extends JPanel implements ITab{
    private ConfigWindow configWindow;

    public EditorTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {

    }
}