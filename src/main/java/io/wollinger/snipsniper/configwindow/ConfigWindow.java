package io.wollinger.snipsniper.configwindow;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.sceditor.stamps.CounterStamp;
import io.wollinger.snipsniper.sceditor.stamps.CubeStamp;
import io.wollinger.snipsniper.sceditor.stamps.IStamp;
import io.wollinger.snipsniper.sceditor.stamps.StampUtils;
import io.wollinger.snipsniper.utils.*;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
    private Config lastSelectedConfig;

    private final String id = "CFGW";

    public enum PAGE {snipPanel, editorPanel, viewerPanel, globalPanel}

    private final int indexSnip = 0;
    private final int indexEditor = 1;
    private final int indexViewer = 2;
    private final int indexGlobal = 3;

    public ConfigWindow(Config config, PAGE page) {
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

        setup(config, page);
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

    public void setup(Config config, PAGE page) {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        final int iconSize = 16;
        int index = 0;
        int enableIndex = 0;

        lastSelectedConfig = config;

        snipConfigPanel = new JPanel();
        tabPane.addTab("SnipSniper Settings",  setupSnipPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_taskbar.getScaledInstance(iconSize, iconSize, 0)));
        if(page == PAGE.snipPanel)
            enableIndex = index;
        index++;

        editorConfigPanel = new JPanel();
        tabPane.addTab("Editor Settings", setupEditorPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_editor.getScaledInstance(iconSize,iconSize,0)));
        if(page == PAGE.editorPanel)
            enableIndex = index;
        index++;

        viewerConfigPanel = new JPanel();
        tabPane.addTab("Viewer Settings", setupViewerPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_viewer.getScaledInstance(iconSize,iconSize,0)));
        if(page == PAGE.viewerPanel)
            enableIndex = index;
        index++;

        globalConfigPanel = new JPanel();
        tabPane.addTab("Global Settings", setupGlobalPane());
        tabPane.setIconAt(index, new ImageIcon(Icons.icon_config.getScaledInstance(iconSize, iconSize, 0)));
        if(page == PAGE.globalPanel)
            enableIndex = index;

        tabPane.setSelectedIndex(enableIndex);

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

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
        } else {
            config = new Config("disabled_cfg.cfg", "CFGT", "profile_defaults.cfg");
            disablePage = true;
        }

        JPanel options = new JPanel(new GridLayout(0,1));

        int hGap = 20;

        JPanel configDropdownRow = new JPanel(getGridLayoutWithMargin(0, 1, hGap));
        ArrayList<String> profiles = new ArrayList<>();
        if(configOriginal == null)
            profiles.add("Select a profile");
        for(File file : configFiles) {
            if(file.getName().contains("profile"))
                profiles.add(file.getName().replaceAll(Config.DOT_EXTENSION, ""));
        }
        JComboBox<Object> dropdown = new JComboBox<>(profiles.toArray());
        if(configOriginal == null)
            dropdown.setSelectedIndex(0);
        else
            dropdown.setSelectedItem(config.getFilename().replaceAll(Config.DOT_EXTENSION, ""));
        dropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                snipConfigPanel.removeAll();
                Config newConfig = new Config(e.getItem() + ".cfg", "CFGT", "profile_defaults.cfg");
                tabPane.setComponentAt(0, setupSnipPane(newConfig));
                lastSelectedConfig = newConfig;
            }
        });
        configDropdownRow.add(dropdown);
        options.add(configDropdownRow);

        //BEGIN ELEMENTS

        JPanel row0 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row0.add(createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.RIGHT, JLabel.CENTER));
        JPanel row0_1 = new JPanel(new GridLayout(0,2));
        HotKeyButton hotKeyButton = new HotKeyButton(config.getString(ConfigHelper.PROFILE.hotkey));
        hotKeyButton.addDoneCapturingListener(e -> {
            if(hotKeyButton.hotkey != -1) {
                String hotkeyModifier = "KB";
                if (!hotKeyButton.isKeyboard)
                    hotkeyModifier = "M";
                config.set(ConfigHelper.PROFILE.hotkey, hotkeyModifier + hotKeyButton.hotkey);
            } else {
                config.set(ConfigHelper.PROFILE.hotkey, "NONE");
            }
        });
        row0_1.add(hotKeyButton);
        JButton deleteHotKey = new JButton(LangManager.getItem("config_label_delete"));
        deleteHotKey.addActionListener(e -> {
            hotKeyButton.setText(LangManager.getItem("config_label_none"));
            hotKeyButton.hotkey = -1;
            config.set(ConfigHelper.PROFILE.hotkey, "NONE");
        });
        row0_1.add(deleteHotKey);
        row0.add(row0_1);
        options.add(row0);

        JPanel row1 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row1.add(createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.RIGHT, JLabel.CENTER));
        JCheckBox saveToDisk = new JCheckBox();
        saveToDisk.setSelected(config.getBool(ConfigHelper.PROFILE.saveToDisk));
        saveToDisk.addActionListener(e -> config.set(ConfigHelper.PROFILE.saveToDisk, saveToDisk.isSelected() + ""));
        row1.add(saveToDisk);
        options.add(row1);

        JPanel row2 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row2.add(createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.RIGHT, JLabel.CENTER));
        JCheckBox copyToClipboard = new JCheckBox();
        copyToClipboard.setSelected(config.getBool(ConfigHelper.PROFILE.copyToClipboard));
        copyToClipboard.addActionListener(e -> config.set(ConfigHelper.PROFILE.copyToClipboard, copyToClipboard.isSelected() + ""));
        row2.add(copyToClipboard);
        options.add(row2);

        JPanel row3 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row3.add(createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.RIGHT, JLabel.CENTER));
        JPanel row3_2 = new JPanel(new GridLayout(0,2));
        JSpinner borderSize = new JSpinner(new SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.borderSize), 0.0, maxBorder, 1.0)); //TODO: Extend JSpinner class to notify user of too large number
        borderSize.addChangeListener(e -> config.set(ConfigHelper.PROFILE.borderSize, (int)((double) borderSize.getValue()) + ""));
        row3_2.add(borderSize);
        JButton colorBtn = new JButton(LangManager.getItem("config_label_color"));
        PBRColor borderColor = new PBRColor(config.getColor(ConfigHelper.PROFILE.borderColor));
        borderColor.addChangeListener(e -> config.set(ConfigHelper.PROFILE.borderColor, Utils.rgb2hex((Color)e.getSource())));
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
        //TODO: Add to wiki: if you just enter a word like "images" it will create the folder next to the jar.
        JTextField pictureLocation = new JTextField(config.getRawString(ConfigHelper.PROFILE.pictureFolder));
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
                            config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationFinal);
                        }
                    }
                } else {
                    allowSaving[0] = true;
                    config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationFinal);
                }
            }
        });
        row4.add(pictureLocation);
        options.add(row4);

        JPanel row5 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row5.add(createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.RIGHT, JLabel.CENTER));
        JPanel row5_2 = new JPanel(new GridLayout(0,2));
        JSpinner snipeDelay = new JSpinner(new SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.snipeDelay), 0.0, 100, 1.0));
        snipeDelay.addChangeListener(e -> config.set(ConfigHelper.PROFILE.snipeDelay, (int)((double) snipeDelay.getValue()) + ""));
        row5_2.add(snipeDelay);
        row5.add(row5_2);
        options.add(row5);

        JPanel row6 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row6.add(createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.RIGHT, JLabel.CENTER));
        JCheckBox openEditor = new JCheckBox();
        openEditor.setSelected(config.getBool(ConfigHelper.PROFILE.openEditor));
        openEditor.addActionListener(e -> config.set(ConfigHelper.PROFILE.openEditor, openEditor.isSelected() + ""));

        row6.add(openEditor);
        options.add(row6);

        //END ELEMENTS

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
                //This prevents a bug where the other tabs have an outdated config
                tabPane.setComponentAt(indexEditor, setupEditorPane(configOriginal));
                tabPane.setComponentAt(indexViewer, setupViewerPane(configOriginal));
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

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(snipConfigPanel);
    }

    public JComponent setupEditorPane(Config configOriginal) {
        editorConfigPanel.removeAll();
        final boolean[] allowSaving = {true};

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
        } else {
            config = new Config("disabled_cfg.cfg", "CFGT", "profile_defaults.cfg");
            disablePage = true;
        }

        JPanel options = new JPanel(new GridBagLayout());

        ArrayList<String> profiles = new ArrayList<>();
        if(configOriginal == null)
            profiles.add("Select a profile");
        for(File file : configFiles) {
            if(file.getName().contains("profile") || file.getName().contains("editor"))
                profiles.add(file.getName().replaceAll(Config.DOT_EXTENSION, ""));
        }
        JComboBox<Object> dropdown = new JComboBox<>(profiles.toArray());
        if(configOriginal == null)
            dropdown.setSelectedIndex(0);
        else
            dropdown.setSelectedItem(config.getFilename().replaceAll(Config.DOT_EXTENSION, ""));
        dropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                editorConfigPanel.removeAll();
                Config newConfig = new Config(e.getItem() + ".cfg", "CFGT", "profile_defaults.cfg");
                tabPane.setComponentAt(1, setupEditorPane(newConfig));
                lastSelectedConfig = newConfig;
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1F;
        options.add(dropdown, gbc);
        //BEGIN ELEMENTS

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 10, 0, 10);
        options.add(createJLabel("Smart Pixel", JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox smartPixelCheckBox = new JCheckBox();
        smartPixelCheckBox.setSelected(config.getBool(ConfigHelper.PROFILE.smartPixel));
        smartPixelCheckBox.addActionListener(e -> config.set(ConfigHelper.PROFILE.smartPixel, smartPixelCheckBox.isSelected() + ""));
        gbc.gridx = 1;
        options.add(smartPixelCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        options.add(createJLabel("HSV color switch speed", JLabel.RIGHT, JLabel.CENTER), gbc);
        JLabel hsvPercentage = new JLabel(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed) + "%");
        hsvPercentage.setHorizontalAlignment(JLabel.CENTER);
        JSlider hsvSlider = new JSlider(JSlider.HORIZONTAL);
        hsvSlider.setMinimum(-100);
        hsvSlider.setMaximum(100);
        hsvSlider.setSnapToTicks(true);
        hsvSlider.addChangeListener(e -> {
            hsvPercentage.setText(hsvSlider.getValue() + "%");
            config.set(ConfigHelper.PROFILE.hsvColorSwitchSpeed, hsvSlider.getValue() + "");
        });

        hsvSlider.setValue(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed));
        gbc.gridx = 1;
        options.add(hsvSlider, gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        options.add(hsvPercentage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets.top = 20;
        JPanel row3_stampConfig = new JPanel(getGridLayoutWithMargin(0, 2, 20));
        StampJPanel row3_stampPreview = new StampJPanel();
        IStamp stamp = new CubeStamp(config, null);
        row3_stampPreview.setStamp(stamp);
        setupStampConfigPanel(row3_stampConfig, stamp, row3_stampPreview, config);
        JComboBox<Object> stampDropdown = new JComboBox<>(StampUtils.getStampsAsString());
        stampDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IStamp newStamp = StampUtils.getNewIStampByIndex(stampDropdown.getSelectedIndex(), config, null);
                row3_stampPreview.setStamp(newStamp);
                setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config);
            }
        });

        options.add(stampDropdown, gbc);
        gbc.gridx = 1;
        options.add(createJLabel("Preview", JLabel.CENTER, JLabel.BOTTOM), gbc);
        gbc.gridx = 0;
        gbc.insets.top = 0;
        gbc.gridy = 5;
        options.add(row3_stampConfig, gbc);
        gbc.gridx = 1;
        options.add(row3_stampPreview, gbc);

        //END ELEMENTS

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
                //This prevents a bug where the other tabs have an outdated config
                tabPane.setComponentAt(indexSnip, setupSnipPane(configOriginal));
                tabPane.setComponentAt(indexViewer, setupViewerPane(configOriginal));
            }
        });

        gbc.gridy = 6;
        gbc.gridx = 0;
        gbc.insets.top = 20;
        options.add(saveButton, gbc);
        gbc.gridx = 1;
        options.add(saveAndClose, gbc);

        editorConfigPanel.add(options);

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(editorConfigPanel);
    }

    private JSpinner setupStampConfigPanelSpinner(Enum configKey, int min, int max, StampJPanel previewPanel, Config config) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(config.getInt(configKey), min, max, 1));
        spinner.addChangeListener(e -> {
            config.set(configKey, spinner.getValue() + "");
            previewPanel.setStamp(new CubeStamp(config, null));
        });
        return spinner;
    }

    private void setupStampConfigPanelSpinnerWithLabel(JPanel panel, String title, Enum configKey, int min, int max, StampJPanel previewPanel, Config config) {
        panel.add(createJLabel(title, JLabel.RIGHT, JLabel.CENTER));
        panel.add(setupStampConfigPanelSpinner(configKey, min, max, previewPanel, config));
    }

    private JButton setupColorButton(String title, Config config, Enum configKey, ChangeListener whenChange) {
        JButton colorButton = new JButton(title);
        Color startColor = config.getColor(configKey);
        PBRColor startColorPBR = new PBRColor(startColor);
        startColorPBR.addChangeListener(e -> {
            config.set(configKey, Utils.rgb2hex(startColorPBR.getColor()));
            colorButton.setBackground(startColorPBR.getColor());
            colorButton.setForeground(Utils.getContrastColor(startColorPBR.getColor()));
        });
        startColorPBR.addChangeListener(whenChange);
        colorButton.setBackground(startColor);
        colorButton.setForeground(Utils.getContrastColor(startColor));
        colorButton.addActionListener(e -> {
            new ColorChooser(config, "Stamp color", startColorPBR, null, (int) (getLocation().getX() + getWidth() / 2), (int) (getLocation().getY() + getHeight() / 2));
        });
        return colorButton;
    }

    private void setupStampConfigPanel(JPanel panel, IStamp stamp, StampJPanel previewPanel, Config config) {
        panel.removeAll();

        if(stamp instanceof CubeStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER));
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCubeDefaultColor, e -> previewPanel.setStamp(new CubeStamp(config, null)));
            panel.add(colorButton);

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, previewPanel, config);
        } else if(stamp instanceof CounterStamp) {
            panel.add(createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER));
            for (int i = 0; i < 5; i++) panel.add(new JLabel());
        } else {
            panel.add(createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER));
            for (int i = 0; i < 5; i++) panel.add(new JLabel());
        }
    }

    public JComponent setupViewerPane(Config config) {
        viewerConfigPanel.removeAll();
        viewerConfigPanel.setLayout(new BoxLayout(viewerConfigPanel, BoxLayout.PAGE_AXIS));
        JLabel label = new JLabel("<html><h1>Coming soon</h1></html>");
        label.setHorizontalAlignment(JLabel.CENTER);
        viewerConfigPanel.add(label);
        return generateScrollPane(viewerConfigPanel);
    }

    public JComponent setupGlobalPane() {
        globalConfigPanel.setLayout(new MigLayout("align 50% 0%"));

        int hGap = 20;

        JPanel options = new JPanel(new GridLayout(0,1));

        Config config = new Config(SnipSniper.getConfig());

        ArrayList<String> translatedLanguages = new ArrayList<>();
        for(String lang : LangManager.languages)
            translatedLanguages.add(LangManager.getItem(lang, "lang_" + lang));
        JComboBox<Object> languageDropdown = new JComboBox<>(translatedLanguages.toArray());
        languageDropdown.setSelectedIndex(LangManager.languages.indexOf(config.getString(ConfigHelper.MAIN.language)));
        languageDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                config.set(ConfigHelper.MAIN.language, LangManager.languages.get(languageDropdown.getSelectedIndex()));
            }
        });

        JPanel row0 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row0.add(createJLabel("Language", JLabel.RIGHT, JLabel.CENTER));
        row0.add(languageDropdown);
        options.add(row0);

        String[] themes = {"Light Mode", "Dark Mode"};
        JComboBox<Object> themeDropdown = new JComboBox<>(themes);
        int themeIndex = 0; //Light theme
        if(config.getString(ConfigHelper.MAIN.theme).equals("dark"))
            themeIndex = 1;
        themeDropdown.setSelectedIndex(themeIndex);
        themeDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if(themeDropdown.getSelectedIndex() == 0) {
                    config.set(ConfigHelper.MAIN.theme, "light");
                } else if(themeDropdown.getSelectedIndex() == 1) {
                    config.set(ConfigHelper.MAIN.theme, "dark");
                }
            }
        });

        JPanel row1 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row1.add(createJLabel("Theme", JLabel.RIGHT, JLabel.CENTER));
        row1.add(themeDropdown);
        options.add(row1);

        JPanel row2 = new JPanel(getGridLayoutWithMargin(0, 2, hGap));
        row2.add(createJLabel("Debug Mode", JLabel.RIGHT, JLabel.CENTER));
        JCheckBox debugCheckBox = new JCheckBox();
        debugCheckBox.setSelected(config.getBool(ConfigHelper.MAIN.debug));
        debugCheckBox.addActionListener(e -> config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected() + ""));
        row2.add(debugCheckBox);
        options.add(row2);

        JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
        saveButton.addActionListener(e -> {
            boolean restartConfig = !config.getString(ConfigHelper.MAIN.language).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.language));
            boolean didThemeChange = !config.getString(ConfigHelper.MAIN.theme).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme));

            globalSave(config);

            if(restartConfig || didThemeChange) {
                new ConfigWindow(lastSelectedConfig, PAGE.globalPanel);
                close();
            }
        });

        JButton saveAndClose = new JButton("Save and close");
        saveAndClose.addActionListener(e -> {
            globalSave(config);

            for(CustomWindowListener listener : listeners)
                listener.windowClosed();
            close();
        });

        GridLayout layout = new GridLayout(0,4);
        layout.setHgap(hGap);
        JPanel saveRow = new JPanel(layout);
        saveRow.add(new JPanel());
        saveRow.add(saveButton);
        saveRow.add(saveAndClose);
        options.add(saveRow);

        globalConfigPanel.add(options);

        return generateScrollPane(globalConfigPanel);
    }

    private void globalSave(Config config) {
        boolean doRestartProfiles = !config.getString(ConfigHelper.MAIN.language).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.language));
        boolean didThemeChange = !config.getString(ConfigHelper.MAIN.theme).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme));
        boolean didDebugChange = config.getBool(ConfigHelper.MAIN.debug) != SnipSniper.getConfig().getBool(ConfigHelper.MAIN.debug);

        if(didDebugChange && config.getBool(ConfigHelper.MAIN.debug)) {
            SnipSniper.openDebugConsole();
            doRestartProfiles = true;
        } else if(didDebugChange && !config.getBool(ConfigHelper.MAIN.debug)){
            SnipSniper.closeDebugConsole();
            doRestartProfiles = true;
        }

        if(didThemeChange) {
            try {
                if (config.getString(ConfigHelper.MAIN.theme).equals("dark")) {
                    UIManager.setLookAndFeel(new FlatDarculaLaf());
                } else if(config.getString(ConfigHelper.MAIN.theme).equals("light")) {
                    UIManager.setLookAndFeel(new FlatIntelliJLaf());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        SnipSniper.getConfig().loadFromConfig(config);
        config.save();
        if(doRestartProfiles)
            SnipSniper.resetProfiles();
    }

    private void setEnabledAll(JComponent component, boolean enabled, JComponent... ignore) {
        setEnableSpecific(component, enabled, ignore);

        for(Component c : component.getComponents()) {
            if(c instanceof JComponent) {
                JComponent cc = (JComponent) c;
                setEnableSpecific(cc, enabled, ignore);
                if (cc.getComponents().length != 0)
                    setEnabledAll(cc, enabled, ignore);
            }
        }
    }

    private void setEnableSpecific(JComponent component, boolean enabled, JComponent... ignore) {
        boolean doDisable = true;
        for(JComponent comp : ignore)
            if(comp == component) {
                doDisable = false;
                break;
            }

        if(doDisable)
            component.setEnabled(enabled);
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

    public void addCustomWindowListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
