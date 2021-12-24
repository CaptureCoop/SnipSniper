package org.snipsniper.sceditor.ezmode;

import javax.swing.*;

import javafx.scene.shape.Circle;
import org.snipsniper.SnipSniper;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.sceditor.stamps.CircleStamp;
import org.snipsniper.sceditor.stamps.IStamp;
import org.snipsniper.sceditor.stamps.RectangleStamp;
import org.snipsniper.sceditor.stamps.TextStamp;
import org.snipsniper.utils.DropdownItem;
import org.snipsniper.utils.Function;
import org.snipsniper.utils.IFunction;

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
        panel.revalidate();
        panel.repaint();
    }

    public void addColorSettings(JPanel panel, IStamp stamp) {
        panel.add(new JLabel("color"));
        Color color = stamp.getColor().getPrimaryColor();
        panel.add(createEZModeSlider(0, 255, color.getRed(), new Function() {
            @Override
            public boolean run(Integer... args) {
                Color cColor = stamp.getColor().getPrimaryColor();
                stamp.getColor().setPrimaryColor(new Color(args[0], cColor.getGreen(), cColor.getBlue()));
                return true;
            }
        }));
        panel.add(createEZModeSlider(0, 255, color.getGreen(), new Function() {
            @Override
            public boolean run(Integer... args) {
                Color cColor = stamp.getColor().getPrimaryColor();
                stamp.getColor().setPrimaryColor(new Color(cColor.getRed(), args[0], cColor.getBlue()));
                return true;
            }
        }));
        panel.add(createEZModeSlider(0, 255, color.getBlue(), new Function() {
            @Override
            public boolean run(Integer... args) {
                Color cColor = stamp.getColor().getPrimaryColor();
                stamp.getColor().setPrimaryColor(new Color(cColor.getRed(), cColor.getGreen(), args[0]));
                return true;
            }
        }));

    }

    public void addWidthHeightSettings(JPanel panel, IStamp stamp) {
        final int boxMinimum = 1;
        final int boxMaximum = 400;
        panel.add(new JLabel("width"));
        panel.add(createEZModeSlider(boxMinimum, boxMaximum, stamp.getWidth(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setWidth(args[0]);
                return true;
            }
        }));
        panel.add(createJSeperator());
        panel.add(new JLabel("height"));
        panel.add(createEZModeSlider(boxMinimum, boxMaximum, stamp.getHeight(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setHeight(args[0]);
                return true;
            }
        }));
        panel.add(createJSeperator());
    }

    public void addBasicBoxSettings(JPanel panel, IStamp stamp) {
        addWidthHeightSettings(panel, stamp);
        addColorSettings(panel, stamp);
    }

    public void addBasicCircleSettings(JPanel panel, IStamp stamp, boolean addColor) {
        panel.add(new JLabel("size"));
        panel.add(createEZModeSlider(1, 400, stamp.getWidth(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setWidth(args[0]);
                stamp.setHeight(args[0]);
                return true;
            }
        }));
        if(!addColor)
            return;
        panel.add(createJSeperator());
        addColorSettings(panel, stamp);
    }

    public JSlider createEZModeSlider(int min, int max, int currentValue, Function onChange) {
        JSlider slider = new JSlider();
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 30);
        slider.setPreferredSize(dim);
        slider.setMinimumSize(dim);
        slider.setMaximumSize(dim);

        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setValue(currentValue);

        slider.addChangeListener(e -> {
            onChange.run(slider.getValue());
            scEditorWindow.requestFocus();
        });
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
        addBasicCircleSettings(panel, stamp, true);
    }

    private void circle(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp, false);
        CircleStamp cStamp = (CircleStamp)stamp;
        panel.add(new JLabel("thickness"));
        panel.add(createEZModeSlider(1, 200, cStamp.getThickness(), new Function() {
            @Override
            public boolean run(Integer... args) {
                cStamp.setThickness(args[0]);
                return true;
            }
        }));
        panel.add(createJSeperator());
        addColorSettings(panel, stamp);
    }

    private void brush(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp, true);
    }

    private void text(JPanel panel, IStamp stamp) {
        panel.add(new JLabel("font size"));
        //Font size = height
        panel.add(createEZModeSlider(5, 200, stamp.getHeight(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setHeight(args[0]);
                return true;
            }
        }));
        panel.add(createJSeperator());
        TextStamp textStamp = (TextStamp) stamp;
        panel.add(new JLabel("font type"));
        JComboBox<DropdownItem> fontTypeDropdown = new JComboBox<>();
        fontTypeDropdown.addItem(new DropdownItem("plain", "plain"));
        fontTypeDropdown.addItem(new DropdownItem("bold", "bold"));
        fontTypeDropdown.addItem(new DropdownItem("italic", "italic"));
        Dimension dim = new Dimension(scEditorWindow.getEzModeWidth(), 30);
        fontTypeDropdown.setMinimumSize(dim);
        fontTypeDropdown.setMaximumSize(dim);
        fontTypeDropdown.setPreferredSize(dim);

        switch(textStamp.getFontMode()) {
            case Font.PLAIN: fontTypeDropdown.setSelectedIndex(0); break;
            case Font.BOLD: fontTypeDropdown.setSelectedIndex(1); break;
            case Font.ITALIC: fontTypeDropdown.setSelectedIndex(2); break;
        }

        //If we idle for more then 5 seconds remove focus
        fontTypeDropdown.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                super.focusGained(focusEvent);
                SnipSniper.getNewThread(args -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    scEditorWindow.requestFocus();
                }).start();
            }
        });
        fontTypeDropdown.addItemListener(e -> {
            textStamp.setFontMode(fontTypeDropdown.getSelectedIndex());
            scEditorWindow.requestFocus();
        });

        panel.add(fontTypeDropdown);
    }

    private void rectangle(JPanel panel, IStamp stamp) {
        addWidthHeightSettings(panel, stamp);
        RectangleStamp rStamp = (RectangleStamp)stamp;
        panel.add(new JLabel("thickness"));
        panel.add(createEZModeSlider(1, 200, rStamp.getThickness(), new Function() {
            @Override
            public boolean run(Integer... args) {
                rStamp.setThickness(args[0]);
                return true;
            }
        }));
        panel.add(createJSeperator());
        addColorSettings(panel, stamp);
    }

    private void eraser(JPanel panel, IStamp stamp) {
        addBasicCircleSettings(panel, stamp, false);
    }
}
