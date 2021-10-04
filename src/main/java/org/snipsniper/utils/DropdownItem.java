package org.snipsniper.utils;

import javax.swing.*;
import java.awt.*;

public class DropdownItem {
    private final String label;
    private final String id;
    private Icon icon;

    public DropdownItem(String label, String id) {
        this.label = label;
        this.id = id;
    }

    public DropdownItem(String label, String id, Image icon) {
        this.label = label;
        this.id = id;
        this.icon = new ImageIcon(ImageUtils.imageToBufferedImage(icon).getScaledInstance(16, 16, 0));
    }

    public boolean compare(DropdownItem otherItem) {
        return compare(otherItem.getID());
    }

    public boolean compare(String id) {
        return this.id.equals(id);
    }

    public String getLabel() {
        return label;
    }

    public String getID() {
        return id;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return label;
    }

    public static int setSelected(JComboBox<DropdownItem> comboBox, String id) {
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            if(comboBox.getItemAt(i).compare(id)) {
                comboBox.setSelectedIndex(i);
                return i;
            }
        }
        return 0;
    }
}
