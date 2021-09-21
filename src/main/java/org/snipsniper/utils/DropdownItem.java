package org.snipsniper.utils;

import javax.swing.*;

public class DropdownItem {
    private final String label;
    private final String id;

    public DropdownItem(String label, String id) {
        this.label = label;
        this.id = id;
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

    @Override
    public String toString() {
        return label;
    }

    public static void setSelected(JComboBox<DropdownItem> comboBox, String id) {
        for(int i = 0; i < comboBox.getItemCount(); i++) {
            if(comboBox.getItemAt(i).compare(id)) {
                comboBox.setSelectedIndex(i);
                return;
            }
        }
    }
}
