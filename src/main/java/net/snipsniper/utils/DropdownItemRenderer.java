package net.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class DropdownItemRenderer extends DefaultListCellRenderer {
    private final HashMap<String, Icon> images = new HashMap<>();

    public DropdownItemRenderer(DropdownItem[] items) {
        for(DropdownItem item : items)
            images.put(item.getID(), item.getIcon());
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        DropdownItem item = (DropdownItem) value;
        label.setIcon(images.get(item.getID()));
        return label;
    }
}
