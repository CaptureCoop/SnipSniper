package org.snipsniper.configwindow.tabs;

import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;

public interface ITab {
    ConfigWindow.PAGE getPage();
    void setup(Config configOriginal);
    void setDirty(boolean isDirty);
    boolean isDirty();
}
