package org.snipsniper.sceditor.ezmode;

import javax.swing.*;

import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.sceditor.stamps.IStamp;
import org.snipsniper.utils.DropdownItem;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class EzModeSettingsCreator {
    private final SCEditorWindow scEditorWindow;

    public EzModeSettingsCreator(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
    }

    public void addSettingsToPanel(JPanel panel, IStamp stamp) {
        panel.removeAll();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(createJSeperator());
        switch(stamp.getType()) {
            case CUBE: cube(panel, stamp); break;
            case COUNTER: counter(panel, stamp); break;
            case CIRCLE: circle(panel, stamp); break;
            case BRUSH: brush(panel, stamp); break;
            case TEXT: text(panel, stamp); break;
            case RECTANGLE: rectangle(panel, stamp); break;
            case ERASER: eraser(panel, stamp); break;
        }
        panel.add(createJSeperator());
        panel.repaint();
    }

    public void addColorSettings(JPanel panel, IStamp stamp) {
        panel.add(new JLabel("color"));
        Color color = stamp.getColor().getPrimaryColor();
        panel.add(createEZModeSlider(0, 255, color.getRed()));
        panel.add(createEZModeSlider(0, 255, color.getGreen()));
        panel.add(createEZModeSlider(0, 255, color.getBlue()));
    }

    public void addBasicBoxSettings(JPanel panel, IStamp stamp) {
        final int boxMinimum = 1;
        final int boxMaximum = 400;
        panel.add(new JLabel("width"));
        panel.add(createEZModeSlider(boxMinimum, boxMaximum, stamp.getWidth()));
        panel.add(createJSeperator());
        panel.add(new JLabel("height"));
        panel.add(createEZModeSlider(boxMinimum, boxMaximum, stamp.getHeight()));
        panel.add(createJSeperator());
        addColorSettings(panel, stamp);
    }

    public void addBasicCircleSettings(JPanel panel, IStamp stamp) {
        panel.add(new JLabel("size"));
        panel.add(createEZModeSlider(1, 400, stamp.getWidth()));
        panel.add(createJSeperator());
        addColorSettings(panel, stamp);
    }

    public JSlider createEZModeSlider(int min, int max, int currentValue) {
        JSlider slider = new JSlider();
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 30);
        slider.setPreferredSize(dim);
        slider.setMinimumSize(dim);
        slider.setMaximumSize(dim);

        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(currentValue);

        slider.addChangeListener(e -> scEditorWindow.requestFocus());
        slider.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                scEditorWindow.requestFocus();
            }
        });

        return slider;
    }

    public JSeparator createJSeperator() {
        JSeparator sep = new JSeparator();
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 10);
        sep.setPreferredSize(dim);
        sep.setMinimumSize(dim);
        sep.setMaximumSize(dim);
        return sep;
    }

    private void cube(JPanel panel, IStamp stamp) {
        addBasicBoxSettings(panel, stamp);
    }

    private void counter(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp);
    }

    private void circle(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp);
    }

    private void brush(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp);
    }

    private void text(JPanel panel, IStamp stamp) {
        panel.add(new JLabel("font size"));
        //Font size = height
        panel.add(createEZModeSlider(5, 200, stamp.getHeight()));
        panel.add(createJSeperator());
        panel.add(new JLabel("font type"));
        JComboBox<DropdownItem> fontTypeDropdown = new JComboBox<>();
        fontTypeDropdown.addItem(new DropdownItem("plain", "plain"));
        fontTypeDropdown.addItem(new DropdownItem("bold", "bold"));
        fontTypeDropdown.addItem(new DropdownItem("italic", "italic"));
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 30);
        fontTypeDropdown.setMinimumSize(dim);
        fontTypeDropdown.setMaximumSize(dim);
        fontTypeDropdown.setPreferredSize(dim);
        panel.add(fontTypeDropdown);
    }

    private void rectangle(JPanel panel, IStamp stamp) {
        addBasicBoxSettings(panel, stamp);
    }

    private void eraser(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp);
    }
}
