package org.snipsniper.sceditor.ezmode;

import javax.swing.*;

import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.sceditor.stamps.IStamp;

import java.awt.*;

public class EzModeSettingsCreator {
    private SCEditorWindow scEditorWindow;

    public EzModeSettingsCreator(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
    }

    public void addSettingsToPanel(JPanel panel, IStamp stamp) {
        panel.removeAll();
        switch(stamp.getType()) {
            case CUBE: cube(panel, stamp); break;
            case COUNTER: counter(panel, stamp); break;
            case CIRCLE: circle(panel, stamp); break;
            case BRUSH: brush(panel, stamp); break;
            case TEXT: text(panel, stamp); break;
            case RECTANGLE: rectangle(panel, stamp); break;
            case ERASER: eraser(panel, stamp); break;
        }
        panel.repaint();
    }

    public JSlider createEZModeSlider() {
        JSlider slider = new JSlider();
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 30);
        slider.setPreferredSize(dim);
        slider.setMinimumSize(dim);
        slider.setMaximumSize(dim);
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
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("width"));
        panel.add(createEZModeSlider());
        panel.add(createJSeperator());
        panel.add(new JLabel("height"));
        panel.add(createEZModeSlider());
        panel.add(createJSeperator());
        panel.add(new JLabel("color"));
        panel.add(createEZModeSlider());
        panel.add(createEZModeSlider());
        panel.add(createEZModeSlider());
    }

    private void counter(JPanel panel, IStamp stamp) {
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("size"));
        panel.add(createEZModeSlider());
        panel.add(createJSeperator());
        panel.add(new JLabel("color"));
        panel.add(createEZModeSlider());
        panel.add(createEZModeSlider());
        panel.add(createEZModeSlider());
    }

    private void circle(JPanel panel, IStamp stamp) {

    }

    private void brush(JPanel panel, IStamp stamp) {

    }

    private void text(JPanel panel, IStamp stamp) {

    }

    private void rectangle(JPanel panel, IStamp stamp) {

    }

    private void eraser(JPanel panel, IStamp stamp) {

    }

}
