package net.snipsniper.configwindow.iconwindow;

import net.snipsniper.ImageManager;
import net.snipsniper.SnipSniper;
import net.snipsniper.utils.FileUtils;
import net.snipsniper.utils.IDJButton;
import net.snipsniper.utils.IFunction;
import net.snipsniper.utils.SSFile;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class IconButton extends IDJButton {
    private final SSFile.LOCATION location;
    private boolean onRedX = false;
    private final int size = 32;

    private IFunction onSelect;
    private IFunction onDelete;

    public IconButton(String id, SSFile.LOCATION location) {
        super(id);
        this.location = location;
        addActionListener(e -> {
            if(onRedX && location == SSFile.LOCATION.LOCAL) {
                FileUtils.delete(SnipSniper.Companion.getImgFolder() + "/" + new SSFile(getID()).getPath());
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

    public void setOnSelect(IFunction function) {
        onSelect = function;
    }

    public void setOnDelete(IFunction function) {
        onDelete = function;
    }

    public void paint(Graphics g) {
        super.paint(g);
        if(location == SSFile.LOCATION.LOCAL) {
            g.drawImage(ImageManager.Companion.getImage("icons/redx.png"), getWidth() - size, 0, size, size, this);
        }
    }
}
