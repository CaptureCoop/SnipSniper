package net.snipsniper.sceditor.ezmode;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.snipsniper.utils.GradientJButton;
import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.cccolorutils.chooser.CCColorChooser;
import org.capturecoop.cccolorutils.chooser.gui.CCHSBHueBar;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.configwindow.StampJPanel;
import net.snipsniper.sceditor.SCEditorWindow;
import net.snipsniper.sceditor.stamps.*;
import net.snipsniper.utils.DropdownItem;
import net.snipsniper.utils.Function;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.ccutils.utils.CCIClosable;

import java.awt.*;
import java.awt.event.*;

public class EzModeSettingsCreator {
    private final SCEditorWindow scEditorWindow;
    private StampJPanel stampPreviewPanel;
    private JPanel lastPanel;

    public EzModeSettingsCreator(SCEditorWindow scEditorWindow) {
        this.scEditorWindow = scEditorWindow;
    }

    public void addSettingsToPanel(JPanel panel, IStamp stamp, int width) {
        panel.removeAll();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(createJSeperator());
        switch(stamp.getType()) {
            case CUBE: cube(panel, stamp, width); break;
            case COUNTER: counter(panel, stamp, width); break;
            case CIRCLE: circle(panel, stamp, width); break;
            case SIMPLE_BRUSH: brush(panel, stamp, width); break;
            case TEXT: text(panel, stamp, width); break;
            case RECTANGLE: rectangle(panel, stamp, width); break;
            case ERASER: eraser(panel, stamp, width); break;
        }
        panel.add(createJSeperator());
        panel.add(new JLabel("preview"));
        stampPreviewPanel = new StampJPanel();
        stampPreviewPanel.setMargin(10);
        stampPreviewPanel.setStamp(stamp);
        stampPreviewPanel.setBackground(scEditorWindow.getOriginalImage());
        Dimension previewDimension = new Dimension(scEditorWindow.getEzModeWidth(), scEditorWindow.getEzModeWidth());
        stampPreviewPanel.setPreferredSize(previewDimension);
        stampPreviewPanel.setMinimumSize(previewDimension);
        stampPreviewPanel.setMaximumSize(previewDimension);

        panel.add(stampPreviewPanel);
        panel.revalidate();
        panel.repaint();
        lastPanel = panel;
    }

    public int getLastCorrectHeight() {
        int height = 0;
        for(Component comp : lastPanel.getComponents()) {
            if(!(comp instanceof CCHSBHueBar))
                height += comp.getHeight();
        }
        return height;
    }

    public void addColorSettings(JPanel panel, IStamp stamp, int width) {
        CCColor stampColor = stamp.getColor();
        JPanel cPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cPanel.setPreferredSize(new Dimension(width, 40));
        GradientJButton button = new GradientJButton("Color", stampColor);
        button.setPreferredSize(new Dimension(width / 2, 30));
        button.addActionListener(e -> {
            int x = scEditorWindow.getLocation().x + scEditorWindow.getWidth() / 2;
            int y = scEditorWindow.getLocation().y + scEditorWindow.getHeight() / 2;

            CCIClosable closable = new CCColorChooser(stampColor, "Stamp color", x, y, true, scEditorWindow.getOriginalImage(), null);
            scEditorWindow.addClosableWindow(closable);
        });

        cPanel.add(button);
        panel.add(cPanel);

        stampColor.addChangeListener(e -> scEditorWindow.repaint());
    }

    public void addWidthHeightSettings(JPanel panel, IStamp stamp) {
        final int boxMinimum = 1;
        final int boxMaximum = 400;
        panel.add(new JLabel("width"));
        JSlider widthSlider = createEZModeSlider(boxMinimum, boxMaximum, stamp.getWidth(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setWidth(args[0]);
                stampPreviewPanel.repaint();
                return true;
            }
        });
        panel.add(widthSlider);
        panel.add(createJSeperator());
        panel.add(new JLabel("height"));
        JSlider heightSlider = createEZModeSlider(boxMinimum, boxMaximum, stamp.getHeight(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setHeight(args[0]);
                stampPreviewPanel.repaint();
                return true;
            }
        });
        panel.add(heightSlider);

        stamp.addChangeListener(type -> {
            if(type == IStampUpdateListener.TYPE.INPUT) {
                widthSlider.setValue(stamp.getWidth());
                heightSlider.setValue(stamp.getHeight());
            }
        });
        panel.add(createJSeperator());
    }

    public void addBasicBoxSettings(JPanel panel, IStamp stamp, int width) {
        addWidthHeightSettings(panel, stamp);
        addColorSettings(panel, stamp, width);
    }

    public void addBasicCircleSettings(JPanel panel, IStamp stamp, boolean addColor, int width) {
        panel.add(new JLabel("size"));
        JSlider sizeSlider = createEZModeSlider(1, 400, stamp.getWidth(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setWidth(args[0]);
                stamp.setHeight(args[0]);
                stampPreviewPanel.repaint();
                return true;
            }
        });
        panel.add(sizeSlider);
        stamp.addChangeListener(type -> {
            if(type == IStampUpdateListener.TYPE.INPUT)
                sizeSlider.setValue(stamp.getWidth());
        });
        if(!addColor)
            return;
        panel.add(createJSeperator());
        addColorSettings(panel, stamp, width);
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
            stampPreviewPanel.repaint();
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

    private void cube(JPanel panel, IStamp stamp, int width) {
        addBasicBoxSettings(panel, stamp, width);
    }

    private void counter(JPanel panel, IStamp stamp, int width) {
        addBasicCircleSettings(panel, stamp, true, width);
    }

    private void circle(JPanel panel, IStamp stamp, int width) {
        addBasicCircleSettings(panel, stamp, false, width);
        CircleStamp cStamp = (CircleStamp)stamp;
        panel.add(new JLabel("thickness"));
        JSlider thicknessSlider = createEZModeSlider(1, 200, cStamp.getThickness(), new Function() {
            @Override
            public boolean run(Integer... args) {
                cStamp.setThickness(args[0]);
                return true;
            }
        });
        panel.add(thicknessSlider);
        panel.add(createJSeperator());
        stamp.addChangeListener(type -> {
            if(type == IStampUpdateListener.TYPE.INPUT)
                thicknessSlider.setValue(cStamp.getThickness());
        });
        addColorSettings(panel, stamp, width);
    }

    private void brush(JPanel panel, IStamp stamp, int width) {
        addBasicCircleSettings(panel, stamp, true, width);
    }

    private void text(JPanel panel, IStamp stamp, int width) {
        panel.add(new JLabel("font size"));
        //Font size = height
        JSlider sizeSlider = createEZModeSlider(5, 200, stamp.getHeight(), new Function() {
            @Override
            public boolean run(Integer... args) {
                stamp.setHeight(args[0]);
                return true;
            }
        });
        panel.add(sizeSlider);
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
                SnipSniper.Companion.getNewThread(args -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        CCLogger.log("Error waiting for font type dropdown in ezMode", CCLogLevel.ERROR);
                        CCLogger.logStacktrace(ex, CCLogLevel.ERROR);
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

        panel.add(createJSeperator());

        panel.add(new JLabel("text"));

        JTextField textInput = new JTextField();
        textInput.setMinimumSize(dim);
        textInput.setMaximumSize(dim);
        textInput.setPreferredSize(dim);
        textInput.setText(textStamp.getText());
        textInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.VK_ENTER || keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE)
                    scEditorWindow.requestFocus();
            }
        });
        textInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent focusEvent) {
                textInput.setSelectionStart(0);
                textInput.setSelectionEnd(textInput.getText().length());
            }
        });
        textInput.getDocument().addDocumentListener(new DocumentListener() {
            public void update() {
                textStamp.setText(textInput.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent event) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                update();
            }
        });

        stamp.addChangeListener(type -> {
            if(type == IStampUpdateListener.TYPE.INPUT) {
                sizeSlider.setValue(stamp.getHeight());
                //TODO: Remove code duplication
                switch (textStamp.getFontMode()) {
                    case Font.PLAIN:
                        fontTypeDropdown.setSelectedIndex(0);
                        break;
                    case Font.BOLD:
                        fontTypeDropdown.setSelectedIndex(1);
                        break;
                    case Font.ITALIC:
                        fontTypeDropdown.setSelectedIndex(2);
                        break;
                }
                textInput.setText(textStamp.getText());
            }
        });

        panel.add(textInput);

        addColorSettings(panel, stamp, width);
    }

    private void rectangle(JPanel panel, IStamp stamp, int width) {
        addWidthHeightSettings(panel, stamp);
        RectangleStamp rStamp = (RectangleStamp)stamp;
        panel.add(new JLabel("thickness"));
        JSlider thicknessSlider = createEZModeSlider(1, 200, rStamp.getThickness(), new Function() {
            @Override
            public boolean run(Integer... args) {
                rStamp.setThickness(args[0]);
                return true;
            }
        });
        panel.add(thicknessSlider);
        stamp.addChangeListener(type -> {
            if(type == IStampUpdateListener.TYPE.INPUT)
                thicknessSlider.setValue(rStamp.getThickness());
        });
        panel.add(createJSeperator());
        addColorSettings(panel, stamp, width);
    }

    private void eraser(JPanel panel, IStamp stamp, int width) {
        addBasicCircleSettings(panel, stamp, false, width);
    }
}
