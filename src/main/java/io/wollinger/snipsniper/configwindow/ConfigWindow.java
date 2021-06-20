package io.wollinger.snipsniper.configwindow;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.utils.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

public class ConfigWindow extends JFrame {
    private JTabbedPane tabPane;
    private JPanel snipConfigPanel;
    private JPanel editorConfigPanel;
    private JPanel viewerConfigPanel;
    private JPanel globalConfigPanel;

    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();
    private final ArrayList<File> configFiles = new ArrayList<>();

    private final String id = "CFGW";

    public ConfigWindow(Config config) {
        LogManager.log(id, "Creating config window", Level.INFO);

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Icons.icon_config);
        addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent windowEvent) { }

            @Override
            public void windowClosing(WindowEvent windowEvent) {
                close();
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) { }

            @Override
            public void windowIconified(WindowEvent windowEvent) { }

            @Override
            public void windowDeiconified(WindowEvent windowEvent) { }

            @Override
            public void windowActivated(WindowEvent windowEvent) { }

            @Override
            public void windowDeactivated(WindowEvent windowEvent) { }
        });

        refreshConfigFiles();

        setup(config);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        setSize((int)(snipConfigPanel.getWidth()*1.25F), getHeight());
    }

    public void refreshConfigFiles() {
        File profileFolder = new File(SnipSniper.getProfilesFolder());
        File[] files = profileFolder.listFiles();
        if(files != null) {
            for (File file : files) {
                if (Utils.getFileExtension(file).equals(".cfg"))
                    configFiles.add(file);
            }
        }
    }

    public void setup(Config config) {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

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
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_config.getScaledInstance(iconSize, iconSize, 0)));

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
        snipConfigPanel.setLayout(new MigLayout("align 50% 0%"));

        final boolean[] allowSaving = {true};
        final int maxBorder = 999;
        final ColorChooser[] colorChooser = {null};

        Config config = new Config(configOriginal);

        HotKeyButton hotKeyButton = new HotKeyButton(config.getString("hotkey"));
        hotKeyButton.addDoneCapturingListener(e -> {
            if(hotKeyButton.hotkey != -1) {
                String hotkeyModifier = "KB";
                if (!hotKeyButton.isKeyboard)
                    hotkeyModifier = "M";
                config.set("hotkey", hotkeyModifier + hotKeyButton.hotkey);
            } else {
                config.set("hotkey", "NONE");
            }
        });

        JCheckBox saveToDisk = new JCheckBox();
        saveToDisk.setSelected(config.getBool("saveToDisk"));
        saveToDisk.addActionListener(e -> config.set("saveToDisk", saveToDisk.isSelected() + ""));

        JCheckBox copyToClipboard = new JCheckBox();
        copyToClipboard.setSelected(config.getBool("copyToClipboard"));
        copyToClipboard.addActionListener(e -> config.set("copyToClipboard", copyToClipboard.isSelected() + ""));

        //TODO: Extend JSpinner class to notify user of too large number
        JSpinner borderSize = new JSpinner(new SpinnerNumberModel(config.getInt("borderSize"), 0.0, maxBorder, 1.0));
        borderSize.addChangeListener(e -> config.set("borderSize", (int)((double) borderSize.getValue()) + ""));

        //TODO: Add to wiki: if you just enter a word like "images" it will create the folder next to the jar.
        JTextField pictureLocation = new JTextField(config.getRawString("pictureFolder"));
        pictureLocation.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) { }

            @Override
            public void focusLost(FocusEvent e) {
                String saveLocationFinal = pictureLocation.getText();
                if(!saveLocationFinal.endsWith("/"))
                    saveLocationFinal += "/";

                if(saveLocationFinal.contains("%userprofile%")) saveLocationFinal = saveLocationFinal.replace("%userprofile%", System.getenv("USERPROFILE"));
                if(saveLocationFinal.contains("%username%")) saveLocationFinal = saveLocationFinal.replace("%username%", System.getProperty("user.name"));

                File saveLocationCheck = new File(saveLocationFinal);
                if(!saveLocationCheck.exists()) {
                    allowSaving[0] = false;
                    Object[] options = {"Okay" , LangManager.getItem("config_sanitation_createdirectory") };
                    int msgBox = JOptionPane.showOptionDialog(null,LangManager.getItem("config_sanitation_directory_notexist"), LangManager.getItem("config_sanitation_error"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                    if(msgBox == 1) {
                        allowSaving[0] = new File(saveLocationFinal).mkdirs();

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
        borderColor.addChangeListener(e -> config.set("borderColor", Utils.rgb2hex((Color)e.getSource())));

        JSpinner snipeDelay = new JSpinner(new SpinnerNumberModel(config.getInt("snipeDelay"), 0.0, 100, 1.0));
        snipeDelay.addChangeListener(e -> config.set("snipeDelay", (int)((double) snipeDelay.getValue()) + ""));

        JCheckBox openEditor = new JCheckBox();
        openEditor.setSelected(config.getBool("openEditor"));
        openEditor.addActionListener(e -> config.set("openEditor", openEditor.isSelected() + ""));

        JPanel options = new JPanel(new GridLayout(0,1));

        int hGap = 20;

        JPanel configDropdownRow = new JPanel(getGridLayoutWithMargin(0, 1, hGap));
        ArrayList<String> profiles = new ArrayList<>();
        for(File file : configFiles) {
            if(file.getName().contains("profile"))
                profiles.add(file.getName().replaceAll(Config.DOT_EXTENSION, ""));
        }
        JComboBox<Object> dropdown = new JComboBox<>(profiles.toArray());
        dropdown.setSelectedItem(config.getFilename().replaceAll(Config.DOT_EXTENSION, ""));
        dropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                snipConfigPanel.removeAll();
                Config newConfig = new Config(e.getItem() + ".cfg", "CFGT", "profile_defaults.cfg");
                tabPane.setComponentAt(0, setupSnipPane(newConfig));
            }
        });
        configDropdownRow.add(dropdown);
        options.add(configDropdownRow);

        JPanel row0 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row0.add(createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.RIGHT, JLabel.CENTER));
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

        JPanel row1 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row1.add(createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.RIGHT, JLabel.CENTER));
        row1.add(saveToDisk);
        options.add(row1);

        JPanel row2 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row2.add(createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.RIGHT, JLabel.CENTER));
        row2.add(copyToClipboard);
        options.add(row2);

        JPanel row3 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row3.add(createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.RIGHT, JLabel.CENTER));
        JPanel row3_2 = new JPanel(new GridLayout(0,2));
        row3_2.add(borderSize);
        JButton colorBtn = new JButton(LangManager.getItem("config_label_color"));
        colorBtn.setBackground(borderColor.getColor());
        colorBtn.setForeground(Utils.getContrastColor(borderColor.getColor()));
        colorBtn.addActionListener(e -> {
            if(colorChooser[0] == null || !colorChooser[0].isDisplayable()) {
                int x = (int)((getLocation().getX() + getWidth()/2));
                int y = (int)((getLocation().getY() + getHeight()/2));
                colorChooser[0] = new ColorChooser(config, LangManager.getItem("config_label_bordercolor"), borderColor, null, x, y);
                colorChooser[0].addWindowListener(() -> {
                    colorBtn.setBackground(borderColor.getColor());
                    colorBtn.setForeground(Utils.getContrastColor(borderColor.getColor()));
                });
            }
        });
        row3_2.add(colorBtn);
        row3.add(row3_2);
        options.add(row3);

        JPanel row4 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row4.add(createJLabel(LangManager.getItem("config_label_picturelocation"), JLabel.RIGHT, JLabel.CENTER));
        row4.add(pictureLocation);
        options.add(row4);

        JPanel row5 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row5.add(createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.RIGHT, JLabel.CENTER));
        JPanel row5_2 = new JPanel(new GridLayout(0,2));
        row5_2.add(snipeDelay);
        row5.add(row5_2);
        options.add(row5);

        JPanel row6 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row6.add(createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.RIGHT, JLabel.CENTER));
        row6.add(openEditor);
        options.add(row6);

        JButton saveAndClose = new JButton("Save and close");
        saveAndClose.addActionListener(e -> {
            if(allowSaving[0]) {
                configOriginal.loadFromConfig(config);
                configOriginal.save();
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();
                close();
            }
        });

        JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
        saveButton.addActionListener(e -> {
            if(allowSaving[0]) {
                configOriginal.loadFromConfig(config);
                configOriginal.save();
            }
        });

        GridLayout layout = new GridLayout(0,4);
        layout.setHgap(hGap);
        JPanel row7 = new JPanel(layout);
        row7.add(new JPanel());
        row7.add(saveButton);
        row7.add(saveAndClose);
        options.add(row7);

        snipConfigPanel.add(options);

        return generateScrollPane(snipConfigPanel);
    }

    public JComponent setupEditorPane() {
        editorConfigPanel.setLayout(new BoxLayout(editorConfigPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("<html><h1>Coming soon</h1></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        editorConfigPanel.add(label);
        return generateScrollPane(editorConfigPanel);
    }

    public JComponent setupViewerPane() {
        viewerConfigPanel.setLayout(new BoxLayout(viewerConfigPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("<html><h1>Coming soon</h1></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        viewerConfigPanel.add(label);
        return generateScrollPane(viewerConfigPanel);
    }

    public JComponent setupGlobalPane() {
        globalConfigPanel.setLayout(new BoxLayout(globalConfigPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("<html><h1>Coming soon</h1></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        globalConfigPanel.add(label);
        return generateScrollPane(globalConfigPanel);
    }

    public JLabel createJLabel(String title, int horizontalAlignment, int verticalAlignment) {
        JLabel jlabel = new JLabel(title);
        jlabel.setHorizontalAlignment(horizontalAlignment);
        jlabel.setVerticalAlignment(verticalAlignment);
        return jlabel;
    }

    public GridLayout getGridLayoutWithMargin(int row, int cols, int hGap) {
        GridLayout layout = new GridLayout(row, cols);
        layout.setHgap(hGap);
        return layout;
    }

    public void close() {
        for(CustomWindowListener listener : listeners)
            listener.windowClosed();
        dispose();
    }

    public void addCloseListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
