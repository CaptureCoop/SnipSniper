package org.snipsniper.configwindow.tabs;

import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;

import javax.swing.*;

public class GeneralTab extends JPanel implements ITab{
    private ConfigWindow configWindow;

    public GeneralTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {

    }
}