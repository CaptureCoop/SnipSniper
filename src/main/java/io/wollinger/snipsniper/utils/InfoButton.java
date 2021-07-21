package io.wollinger.snipsniper.utils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class InfoButton extends JButton {
    private String info;

    public InfoButton(String info) {
        this.info = info;
        setText("?");
        addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(info);
            }
        });
    }
}
