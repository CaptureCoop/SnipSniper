package io.wollinger.snipsniper.configwindow;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.SnipSniper;
import io.wollinger.snipsniper.sceditor.stamps.*;
import io.wollinger.snipsniper.systray.Sniper;
import io.wollinger.snipsniper.utils.*;
import net.miginfocom.layout.Grid;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
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

    //TODO:
    //Add "Create" and "Delete" options in profile dropdown? ask moritz
    //Add generic function that handles creation of the profile dropdown

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
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        refreshConfigFiles();

        setup(config, page);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        setSize((int)(snipConfigPanel.getWidth()*1.25F), getHeight());
    }

    public void refreshConfigFiles() {
        configFiles.clear();
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

    public JComponent setupPaneDynamic(Config config, PAGE page) {
        switch(page) {
            case snipPanel: return setupSnipPane(config);
            case editorPanel: return setupEditorPane(config);
            case viewerPanel: return setupViewerPane(config);
            case globalPanel: return setupGlobalPane();
        }
        return null;
    }

    public JComponent setupProfileDropdown(JPanel panelToAdd, JPanel parentPanel, Config configOriginal, Config config, PAGE page, int pageIndex) {
        //Returns the dropdown, however dont add it manually
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
                parentPanel.removeAll();
                Config newConfig = new Config(e.getItem() + ".cfg", "CFGT", "profile_defaults.cfg");
                tabPane.setComponentAt(pageIndex, setupPaneDynamic(newConfig, page));
                lastSelectedConfig = newConfig;
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1F;
        panelToAdd.add(dropdown, gbc);
        gbc.gridx = 2;
        JPanel profilePlusMinus = new JPanel(new GridLayout(0, 2));
        JButton profileAddButton = new JButton("+");
        if(SnipSniper.getProfileCount() == SnipSniper.getProfileCountMax())
            profileAddButton.setEnabled(false);
        profileAddButton.addActionListener(actionEvent -> {
            for(int i = 0; i < SnipSniper.getProfileCountMax(); i++) {
                if(SnipSniper.getProfile(i) == null) {
                    SnipSniper.setProfile(i, new Sniper(i));
                    Config newProfileConfig = SnipSniper.getProfile(i).getConfig();
                    newProfileConfig.save();

                    refreshConfigFiles();
                    parentPanel.removeAll();
                    tabPane.setComponentAt(pageIndex, setupPaneDynamic(newProfileConfig, page));
                    lastSelectedConfig = newProfileConfig;
                    break;
                }
            }
        });
        profilePlusMinus.add(profileAddButton);
        JButton profileRemoveButton = new JButton("-");
        if(dropdown.getSelectedItem().equals("profile0"))
            profileRemoveButton.setEnabled(false);
        profileRemoveButton.addActionListener(actionEvent -> {
            if(!dropdown.getSelectedItem().equals("profile0")) {
                config.deleteFile();
                SnipSniper.resetProfiles();
                refreshConfigFiles();
                parentPanel.removeAll();
                int newIndex = dropdown.getSelectedIndex() - 1;
                if(newIndex < 0)
                    newIndex = dropdown.getSelectedIndex() + 1;
                Config newConfig = new Config(dropdown.getItemAt(newIndex) + ".cfg", "CFGT", "profile_defaults.cfg");
                tabPane.setComponentAt(pageIndex, setupPaneDynamic(newConfig, page));
                lastSelectedConfig = newConfig;
            }

        });
        profilePlusMinus.add(profileRemoveButton);
        panelToAdd.add(profilePlusMinus, gbc);
        return dropdown;
    }

    public JComponent setupSnipPane(Config configOriginal) {
        snipConfigPanel.removeAll();

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
        } else {
            config = new Config("disabled_cfg.cfg", "CFGT", "profile_defaults.cfg");
            disablePage = true;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        JPanel options = new JPanel(new GridBagLayout());

        JComponent dropdown = setupProfileDropdown(options, snipConfigPanel, configOriginal, config, PAGE.snipPanel, indexSnip);



        snipConfigPanel.add(options);

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(snipConfigPanel);
    }

    public JComponent setupEditorPane(Config configOriginal) {
        editorConfigPanel.removeAll();

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
        } else {
            config = new Config("disabled_cfg.cfg", "CFGT", "profile_defaults.cfg");
            disablePage = true;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        JPanel options = new JPanel(new GridBagLayout());

        JComponent dropdown = setupProfileDropdown(options, editorConfigPanel, configOriginal, config, PAGE.editorPanel, indexEditor);
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
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

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
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

        gbc.gridy = 3;
        gbc.gridx = 1;
        options.add(hsvPercentage, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.insets.top = 20;
        JPanel row3_stampConfig = new JPanel(new GridBagLayout());
        StampJPanel row3_stampPreview = new StampJPanel();
        String theme = SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme);
        if(theme.equals("light")) {
            row3_stampPreview.setBackground(Icons.stamp_preview_light);
        } else if(theme.equals("dark")) {
            row3_stampPreview.setBackground(Icons.stamp_preview_dark);
        }
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
        JPanel previewToggleAndLabel = new JPanel(new GridLayout(0,2));
        previewToggleAndLabel.add(createJLabel("Preview", JLabel.RIGHT, JLabel.CENTER));
        JCheckBox previewBGToggle = new JCheckBox();
        previewBGToggle.setSelected(true);
        previewBGToggle.addChangeListener(e -> row3_stampPreview.setBackgroundEnabled(previewBGToggle.isSelected()));
        previewToggleAndLabel.add(previewBGToggle);
        options.add(previewToggleAndLabel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        gbc.gridx = 0;
        gbc.insets.top = 0;
        gbc.gridy = 5;
        options.add(row3_stampConfig, gbc);
        gbc.gridx = 1;
        options.add(row3_stampPreview, gbc);

        //END ELEMENTS

        JButton saveAndClose = new JButton("Save and close");
        saveAndClose.addActionListener(e -> {
            if(configOriginal != null) {
                configOriginal.loadFromConfig(config);
                configOriginal.save();
                for (CustomWindowListener listener : listeners)
                    listener.windowClosed();
                close();
            }
        });

        JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
        saveButton.addActionListener(e -> {
            if(configOriginal != null) {
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

    private JSpinner setupStampConfigPanelSpinner(Enum configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Double.parseDouble(config.getFloat(configKey)+""), min, max, stepSize));
        spinner.addChangeListener(e -> {
            config.set(configKey, spinner.getValue() + "");
            previewPanel.setStamp(StampUtils.getNewIStampByIndex(stampIndex, config, null));
        });
        return spinner;
    }

    private void setupStampConfigPanelSpinnerWithLabel(JPanel panel, String title, Enum configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex, GridBagConstraints constraints, String infoText) {
        constraints.gridx = 0;
        panel.add(createJLabel(title, JLabel.RIGHT, JLabel.CENTER), constraints);
        constraints.gridx = 1;
        panel.add(setupStampConfigPanelSpinner(configKey, min, max, stepSize,previewPanel, config, stampIndex), constraints);
        constraints.gridx = 2;
        panel.add(new InfoButton(infoText), constraints);
        constraints.gridx = 0;
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
        colorButton.addActionListener(e -> new ColorChooser(config, "Stamp color", startColorPBR, null, (int) (getLocation().getX() + getWidth() / 2), (int) (getLocation().getY() + getHeight() / 2)));
        return colorButton;
    }

    private void setupStampConfigPanel(JPanel panel, IStamp stamp, StampJPanel previewPanel, Config config) {
        panel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();

        if(stamp instanceof CubeStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCubeDefaultColor, e -> previewPanel.setStamp(new CubeStamp(config, null)));
            panel.add(colorButton, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCubeHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCubeWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCubeHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCubeWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCubeHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null);
        } else if(stamp instanceof CounterStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER));
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCounterDefaultColor, e -> previewPanel.setStamp(new CounterStamp(config)));
            panel.add(colorButton);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCounterWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCounterHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "General change speed", ConfigHelper.PROFILE.editorStampCounterSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCounterWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCounterHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCounterWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCounterHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);

            panel.add(createJLabel("Solid color", JLabel.RIGHT, JLabel.CENTER), gbc);
            JCheckBox cbSolidColor = new JCheckBox();
            cbSolidColor.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor));
            cbSolidColor.addChangeListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCounterSolidColor, cbSolidColor.isSelected() + "");
                previewPanel.setStamp(new CounterStamp(config));
            });
            gbc.gridx = 1;
            panel.add(cbSolidColor, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            gbc.gridx = 0;
            panel.add(createJLabel("Stamp Border", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JCheckBox cbBorder = new JCheckBox();
            cbBorder.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCounterBorderEnabled));
            cbBorder.addChangeListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCounterBorderEnabled, cbBorder.isSelected() + "");
                previewPanel.setStamp(new CounterStamp(config));
            });
            panel.add(cbBorder, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Font size modifier", ConfigHelper.PROFILE.editorStampCounterFontSizeModifier, 0.1, 10, 0.01D, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Border modifier", ConfigHelper.PROFILE.editorStampCounterBorderModifier, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null);
        } else if(stamp instanceof CircleStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCircleDefaultColor, e -> previewPanel.setStamp(new CircleStamp(config)));
            panel.add(colorButton, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCircleWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCircleHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "General change speed", ConfigHelper.PROFILE.editorStampCircleSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCircleWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCircleHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCircleWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCircleHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Thickness", ConfigHelper.PROFILE.editorStampCircleThickness, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null);
        } else if(stamp instanceof SimpleBrush) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor, e -> previewPanel.setStamp(new SimpleBrush(config, null)));
            panel.add(colorButton, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Brush size", ConfigHelper.PROFILE.editorStampSimpleBrushSize, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Brush size change speed", ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null);
            setupStampConfigPanelSpinnerWithLabel(panel, "Line point distance", ConfigHelper.PROFILE.editorStampSimpleBrushDistance, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null);
            panel.add(new JPanel()); //Padding
        } else if(stamp instanceof TextStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampTextDefaultColor, e -> previewPanel.setStamp(new TextStamp(config, null)));
            panel.add(colorButton, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Default font size", ConfigHelper.PROFILE.editorStampTextDefaultFontSize, 1, 999, 1, previewPanel, config, StampUtils.INDEX_TEXT, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Font size change speed", ConfigHelper.PROFILE.editorStampTextDefaultSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_TEXT, gbc, "text");
            for(int i = 0; i < 6; i++) panel.add(new JPanel(), gbc); //Padding
            //TODO: Draw it in the middle, possibly by giving TextStamp a getTextWidth() function and adding an edgecase to the Stamp Renderer, to move it to the left
        } else if(stamp instanceof RectangleStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JButton colorButton = setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampRectangleDefaultColor, e -> previewPanel.setStamp(new RectangleStamp(config)));
            panel.add(colorButton, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampRectangleWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampRectangleHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampRectangleWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampRectangleHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampRectangleWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampRectangleHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
            setupStampConfigPanelSpinnerWithLabel(panel, "Thickness", ConfigHelper.PROFILE.editorStampRectangleThickness, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text");
        } else {
            panel.add(createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER));
            for (int i = 0; i < 15; i++) panel.add(new JLabel());
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
