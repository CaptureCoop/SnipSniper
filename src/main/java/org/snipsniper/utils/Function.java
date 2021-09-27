package org.snipsniper.utils;

import org.snipsniper.utils.enums.ConfigSaveButtonState;

public abstract class Function {
    public boolean run() { return true; }
    public boolean run(String... args) { return true; }
    public boolean run(Object... args) { return true; }
    public boolean run(Boolean... args) { return true; }
    public boolean run(ConfigSaveButtonState state) { return true; }
}
