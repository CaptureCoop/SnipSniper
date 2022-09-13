package net.snipsniper.utils;

public abstract class Function {
    public boolean run() { return true; }
    public boolean run(String... args) { return true; }
    public boolean run(Object... args) { return true; }
    public boolean run(Boolean... args) { return true; }
    public boolean run(Integer... args) { return true; }
    public boolean run(ConfigSaveButtonState state) { return true; }
}
