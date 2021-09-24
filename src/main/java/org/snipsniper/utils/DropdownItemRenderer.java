package org.snipsniper.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class DropdownItemRenderer extends DefaultListCellRenderer {
    DropdownItem[] items;
    private int lastSelectedIndex = 0;
    public DropdownItemRenderer(DropdownItem[] items) {
        this.items = items;
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if(index == -1) index = lastSelectedIndex;
        if(index >= 0)
            label.setIcon(items[index].getIcon());
        if(isSelected && index != -1) lastSelectedIndex = index;
        System.out.println(value);
        return label;
    }
}
