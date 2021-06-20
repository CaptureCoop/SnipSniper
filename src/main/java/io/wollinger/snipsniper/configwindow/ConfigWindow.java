package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.utils.*;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;

public class ConfigWindow extends JFrame {
    private Config config; //TODO: allow choosing per tab with dropdowns.
    private JPanel globalConfigPanel;
    private JPanel snipConfigPanel;
    private JPanel editorConfigPanel;
    private JPanel viewerConfigPanel;

    public ConfigWindow(Config config, boolean showMain, boolean showEditor, boolean showViewer) {
        this.config = config;

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //TODO: Mark settings as closed so that settings can only be opened once.
        setup(showMain, showEditor, showViewer);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    //TODO: Make sure that only pages related to from where we opened the config window are enabled.
    public void setup(boolean showMain, boolean showEditor, boolean showViewer) {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        final int iconSize = 16;
        int index = 0;

        snipConfigPanel = new JPanel();
        tabPane.addTab("SnipSniper Settings",  setupSnipPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize, iconSize, 0)));
        index++;

        editorConfigPanel = new JPanel();
        tabPane.addTab("Editor Settings", setupEditorPane());
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_editor.getScaledInstance(iconSize,iconSize,0)));
        index++;

        viewerConfigPanel = new JPanel();
        tabPane.addTab("Viewer Settings", setupViewerPane());
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_viewer.getScaledInstance(iconSize,iconSize,0)));
        index++;

        globalConfigPanel = new JPanel();
        tabPane.addTab("Global Settings", setupGlobalPane());
        tabPane.setIconAt(index, new ImageIcon(Icons.icon.getScaledInstance(iconSize, iconSize, 0)));

        //TODO: handle greying out options

        add(tabPane);
    }

    public JScrollPane generateScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    void msgError(String msg) {
        JOptionPane.showMessageDialog(this, msg,LangManager.getItem("config_sanitation_error"), JOptionPane.INFORMATION_MESSAGE);
    }

    public JComponent setupSnipPane(Config configOriginal) {
        snipConfigPanel.removeAll();
        snipConfigPanel.setLayout(new BoxLayout(snipConfigPanel, BoxLayout.PAGE_AXIS));

        final boolean[] allowSaving = {true};
        final int maxBorder = 999;
        final ColorChooser[] colorChooser = {null};

        Config config = new Config(configOriginal);

        HotKeyButton hotKeyButton = new HotKeyButton(config.getString("hotkey"));
        hotKeyButton.addActionListener(e -> config.set("hotkey", hotKeyButton.hotkey + ""));

        JCheckBox saveToDisk = new JCheckBox();
        saveToDisk.setSelected(config.getBool("saveToDisk"));
        saveToDisk.addActionListener(e -> config.set("saveToDisk", saveToDisk.isSelected() + ""));

        JCheckBox copyToClipboard = new JCheckBox();
        copyToClipboard.setSelected(config.getBool("copyToClipboard"));
        copyToClipboard.addActionListener(e -> config.set("copyToClipboard", copyToClipboard.isSelected() + ""));

        //TODO: Extend JSpinner class to notify user of too large number
        JSpinner borderSize = new JSpinner(new SpinnerNumberModel(config.getInt("borderSize"), 0.0, maxBorder, 1.0));
        borderSize.addChangeListener(e -> config.set("borderSize", (int)((double) borderSize.getValue()) + ""));

        JTextField pictureLocation = new JTextField(config.getRawString("pictureFolder"));
        pictureLocation.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                String saveLocationFinal = pictureLocation.getText();
                if(saveLocationFinal.contains("%userprofile%")) saveLocationFinal = saveLocationFinal.replace("%userprofile%", System.getenv("USERPROFILE"));
                if(saveLocationFinal.contains("%username%")) saveLocationFinal = saveLocationFinal.replace("%username%", System.getProperty("user.name"));

                File saveLocationCheck = new File(saveLocationFinal);
                if(!saveLocationCheck.exists()) {
                    allowSaving[0] = false;
                    Object[] options = {"Okay" , LangManager.getItem("config_sanitation_createdirectory") };
                    int msgBox = JOptionPane.showOptionDialog(null,LangManager.getItem("config_sanitation_directory_notexist"), LangManager.getItem("config_sanitation_error"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                    if(msgBox == 1) {
                        File f = new File(saveLocationFinal);
                        allowSaving[0] = f.mkdirs();

                        if(!allowSaving[0]) {
                            msgError(LangManager.getItem("config_sanitation_failed_createdirectory"));
                        } else {
                            config.set("pictureFolder", saveLocationFinal);
                        }
                    }
                } else {
                    allowSaving[0] = true;
                    config.set("pictureFolder", saveLocationFinal);
                }
            }
        });


        PBRColor borderColor = new PBRColor(config.getColor("borderColor"));
        borderColor.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                config.set("borderColor", Utils.rgb2hex((Color)e.getSource()));
            }
        });

        JSpinner snipeDelay = new JSpinner(new SpinnerNumberModel(config.getInt("snipeDelay"), 0.0, 100, 1.0));
        snipeDelay.addChangeListener(e -> config.set("snipeDelay", (int)((double) snipeDelay.getValue()) + ""));

        JCheckBox openEditor = new JCheckBox();
        openEditor.setSelected(config.getBool("openEditor"));
        openEditor.addActionListener(e -> config.set("openEditor", openEditor.isSelected() + ""));

        JPanel options = new JPanel(new GridLayout(0,1));

        JPanel row0 = new JPanel(new GridLayout(0,2));
        row0.add(createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.CENTER, JLabel.CENTER));
        JPanel row0_1 = new JPanel(new GridLayout(0,2));
        row0_1.add(hotKeyButton);
        JButton deleteHotKey = new JButton(LangManager.getItem("config_label_delete"));
        deleteHotKey.addActionListener(e -> {
            hotKeyButton.setText(LangManager.getItem("config_label_none"));
            hotKeyButton.hotkey = -1;
        });
        row0_1.add(deleteHotKey);
        row0.add(row0_1);
        options.add(row0);

        JPanel row1 = new JPanel(new GridLayout(0,2));
        row1.add(createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.CENTER, JLabel.CENTER));
        row1.add(saveToDisk);
        options.add(row1);

        JPanel row2 = new JPanel(new GridLayout(0,2));
        row2.add(createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.CENTER, JLabel.CENTER));
        row2.add(copyToClipboard);
        options.add(row2);

        JPanel row3 = new JPanel(new GridLayout(0,2));
        row3.add(createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.CENTER, JLabel.CENTER));
        JPanel row3_2 = new JPanel(new GridLayout(0,2));
        row3_2.add(borderSize);
        JButton colorBtn = new JButton(LangManager.getItem("config_label_color"));
        colorBtn.addActionListener(e -> {
            if(colorChooser[0] == null || !colorChooser[0].isDisplayable()) {
                int x = (int)((getLocation().getX() + getWidth()/2));
                int y = (int)((getLocation().getY() + getHeight()/2));
                colorChooser[0] = new ColorChooser(config, LangManager.getItem("config_label_bordercolor"), borderColor, null, x, y);
            }
        });
        row3_2.add(colorBtn);
        row3.add(row3_2);
        options.add(row3);

        JPanel row4 = new JPanel(new GridLayout(0,2));
        row4.add(createJLabel(LangManager.getItem("config_label_picturelocation"), JLabel.CENTER, JLabel.CENTER));
        row4.add(pictureLocation);
        options.add(row4);

        JPanel row5 = new JPanel(new GridLayout(0,2));
        row5.add(createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.CENTER, JLabel.CENTER));
        JPanel row5_2 = new JPanel(new GridLayout(0,2));
        row5_2.add(snipeDelay);
        row5.add(row5_2);
        options.add(row5);

        JPanel row6 = new JPanel(new GridLayout(0,2));
        row6.add(createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.CENTER, JLabel.CENTER));
        row6.add(openEditor);
        options.add(row6);

        JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allowSaving[0]) {
                    configOriginal.loadFromConfig(config);
                    configOriginal.save();
                    dispose();
                }
            }
        });

        JPanel row7 = new JPanel(new GridLayout(0,5));
        row7.add(new JPanel());
        row7.add(new JPanel());
        row7.add(saveButton);
        options.add(row7);

        snipConfigPanel.add(options);

        return generateScrollPane(snipConfigPanel);
    }

    public JComponent setupEditorPane() {
        editorConfigPanel.setLayout(new BoxLayout(editorConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            editorConfigPanel.add(new JButton("Editor Config Button"));
        }
        return generateScrollPane(editorConfigPanel);
    }

    public JComponent setupViewerPane() {
        viewerConfigPanel.setLayout(new BoxLayout(viewerConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            viewerConfigPanel.add(new JButton("Viewer Config Button"));
        }
        return generateScrollPane(viewerConfigPanel);
    }

    public JComponent setupGlobalPane() {
        globalConfigPanel.setLayout(new BoxLayout(globalConfigPanel, BoxLayout.PAGE_AXIS));
        for(int i = 0; i < 100; i++) {
            globalConfigPanel.add(new JButton("Global Config Button"));
        }
        return generateScrollPane(globalConfigPanel);
    }

    public JLabel createJLabel(String title, int horizontalAlignment, int verticalAlignment) {
        JLabel jlabel = new JLabel(title);
        jlabel.setHorizontalAlignment(horizontalAlignment);
        jlabel.setVerticalAlignment(verticalAlignment);
        return jlabel;
    }

}
