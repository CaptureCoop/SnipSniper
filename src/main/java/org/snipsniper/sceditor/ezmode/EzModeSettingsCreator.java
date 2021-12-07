package org.snipsniper.sceditor.ezmode;

import javax.swing.*;

import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.sceditor.stamps.IStamp;

import java.awt.*;

public class EzModeSettingsCreator {

    public static void addSettingsToPanel(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {
        panel.removeAll();
        switch(stamp.getType()) {
            case CUBE: cube(editorWindow, panel, stamp); break;
            case COUNTER: counter(editorWindow, panel, stamp); break;
            case CIRCLE: circle(editorWindow, panel, stamp); break;
            case BRUSH: brush(editorWindow, panel, stamp); break;
            case TEXT: text(editorWindow, panel, stamp); break;
            case RECTANGLE: rectangle(editorWindow, panel, stamp); break;
            case ERASER: eraser(editorWindow, panel, stamp); break;
        }
        panel.repaint();
    }

    public static JSlider createEZModeSlider(int width) {
        JSlider slider = new JSlider();
        Dimension dim = new Dimension(width, 30);
        slider.setPreferredSize(dim);
        slider.setMinimumSize(dim);
        slider.setMaximumSize(dim);
        return slider;
    }

    public static JSeparator createJSeperator(int width) {
        JSeparator sep = new JSeparator();
        Dimension dim = new Dimension(width, 10);
        sep.setPreferredSize(dim);
        sep.setMinimumSize(dim);
        sep.setMaximumSize(dim);
        return sep;
    }

    private static void cube(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {
        int ezModeWidth = editorWindow.getEzModeWidth();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("width"));
        panel.add(createEZModeSlider(ezModeWidth));
        panel.add(createJSeperator(ezModeWidth));
        panel.add(new JLabel("height"));
        panel.add(createEZModeSlider(ezModeWidth));
        panel.add(createJSeperator(ezModeWidth));
        panel.add(new JLabel("color"));
        panel.add(createEZModeSlider(ezModeWidth));
        panel.add(createEZModeSlider(ezModeWidth));
        panel.add(createEZModeSlider(ezModeWidth));

    }

    private static void counter(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }

    private static void circle(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }

    private static void brush(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }

    private static void text(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }

    private static void rectangle(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }

    private static void eraser(SCEditorWindow editorWindow, JPanel panel, IStamp stamp) {

    }


}
