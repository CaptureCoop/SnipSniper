package net.snipsniper.configwindow.tabs;

import net.snipsniper.config.Config;
import net.snipsniper.configwindow.ConfigWindow;

public interface ITab {
    ConfigWindow.PAGE getPage();
    void setup(Config configOriginal);
    void setDirty(boolean isDirty);
    boolean isDirty();
}
