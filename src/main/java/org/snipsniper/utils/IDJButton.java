package org.snipsniper.utils;

import javax.swing.*;

public class IDJButton extends JButton {
    private String id;

    public IDJButton(String id) {
        this.id = id;
    }

    public String getID() {
        return id;
    }
}
