package org.snipsniper.configwindow.iconwindow;

import org.snipsniper.SnipSniper;
import org.snipsniper.utils.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends IDJButton {
    private SSFile.LOCATION location;
    private boolean onRedX = false;
    private int size = 32;

    Function onSelect;
    Function onDelete;

    public IconButton(String id, SSFile.LOCATION location) {
        super(id);
        this.location = location;
        addActionListener(e -> {
            if(onRedX && location == SSFile.LOCATION.LOCAL) {
                setIcon(null);
                FileUtils.delete(SnipSniper.getImageFolder() + "/" + new SSFile(getID()).getPath());
                onDelete.run();
            } else {
                onSelect.run();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if(location == SSFile.LOCATION.LOCAL) {
                    onRedX = new Rectangle(getWidth() - size, 0, size, size).contains(e.getPoint());
                }
            }
        });
    }

    public void setOnSelect(Function function) {
        onSelect = function;
    }

    public void setOnDelete(Function function) {
        onDelete = function;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if(location == SSFile.LOCATION.LOCAL) {
            g.drawImage(Icons.getImage("icons/redx.png"), getWidth() - size, 0, size, size, this);
        }
    }
}
