package org.snipsniper.utils;

import javax.swing.*;

public class IDJButton extends JButton {
    private String id;

    public IDJButton() { }

    public IDJButton(String text, String id) {
        setText(text);
        this.id = id;
    }

    public IDJButton(String id) {
        this.id = id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }
}
