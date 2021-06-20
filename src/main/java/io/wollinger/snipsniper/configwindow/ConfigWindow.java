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

public class ConfigWindow extends JFrame {
    private Config config; //TODO: allow choosing per tab with dropdowns.
    private JPanel globalConfigPanel;
    private JPanel snipConfigPanel;
    private JPanel editorConfigPanel;
    private JPanel viewerConfigPanel;

    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();
    private ArrayList<File> configFiles = new ArrayList<>();

    public ConfigWindow(Config config, boolean showMain, boolean showEditor, boolean showViewer) {
        this.config = config;

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
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

        setup(showMain, showEditor, showViewer);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
    }

    public void refreshConfigFiles() {
        File profileFolder = new File(SnipSniper.getProfilesFolder());
        File[] files = profileFolder.listFiles();
        for(File file : files) {
            if(Utils.getFileExtension(file).equals(".cfg"))
                configFiles.add(file);
        }
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
        snipConfigPanel.setLayout(new MigLayout("align 50% 0%"));

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
                profiles.add(file.getName().replaceAll(".cfg", ""));
        }
        JComboBox dropdown = new JComboBox(profiles.toArray());
        dropdown.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                System.out.println(e.getItem());
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

        JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
        saveButton.addActionListener(e -> {
            if(allowSaving[0]) {
                configOriginal.loadFromConfig(config);
                configOriginal.save();
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();
                close();
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

    public GridLayout getGridLayoutWithMargin(int row, int cols, int hGap) {
        GridLayout layout = new GridLayout(row, cols);
        layout.setHgap(hGap);
        return layout;
    }

    public void close() {
        dispose();
    }

    public void addCloseListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
