package org.snipsniper.configwindow.tabs;

import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;

import javax.swing.*;

public class GlobalTab extends JPanel implements ITab{
    private ConfigWindow configWindow;

    public GlobalTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {

    }
}
