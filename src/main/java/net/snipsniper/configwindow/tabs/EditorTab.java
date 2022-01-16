package net.snipsniper.configwindow.tabs;

import net.snipsniper.ImageManager;
import net.snipsniper.LangManager;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.configwindow.ConfigWindow;
import net.snipsniper.configwindow.StampJPanel;
import net.snipsniper.sceditor.stamps.*;
import net.snipsniper.utils.Function;
import net.snipsniper.utils.InfoButton;
import net.snipsniper.utils.enums.ConfigSaveButtonState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class EditorTab extends JPanel implements ITab{
    private final ConfigWindow configWindow;
    private boolean isDirty;

    public EditorTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {
        removeAll();
        isDirty = false;

        final Function[] saveButtonUpdate = {null};

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
            if(configOriginal.getFilename().contains("viewer"))
                disablePage = true;
        } else {
            config = new Config("disabled_cfg.cfg", "profile_defaults.cfg");
            disablePage = true;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);

        JPanel options = new JPanel(new GridBagLayout());

        JComponent dropdown = configWindow.setupProfileDropdown(options, this, configOriginal, config, ConfigWindow.PAGE.editorPanel, "viewer");
        //BEGIN ELEMENTS

        gbc.gridx = 0;
        options.add(configWindow.createJLabel("EzMode", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox ezModeCheckBox = new JCheckBox();
        ezModeCheckBox.setSelected(config.getBool(ConfigHelper.PROFILE.ezMode));
        ezModeCheckBox.addChangeListener(e -> config.set(ConfigHelper.PROFILE.ezMode, ezModeCheckBox.isSelected()));
        options.add(ezModeCheckBox, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_hsvspeed"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JLabel hsvPercentage = new JLabel(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed) + "%");
        hsvPercentage.setHorizontalAlignment(JLabel.CENTER);
        JSlider hsvSlider = new JSlider(JSlider.HORIZONTAL);
        hsvSlider.setMinimum(-100);
        hsvSlider.setMaximum(100);
        hsvSlider.setSnapToTicks(true);
        hsvSlider.addChangeListener(e -> {
            hsvPercentage.setText(hsvSlider.getValue() + "%");
            config.set(ConfigHelper.PROFILE.hsvColorSwitchSpeed, hsvSlider.getValue() + "");
            if(saveButtonUpdate[0] != null)
                saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });

        hsvSlider.setValue(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed));
        gbc.gridx = 1;
        options.add(hsvSlider, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

        gbc.gridx = 0;
        options.add(new JPanel(), gbc); //Avoid shifting around things
        gbc.gridx = 1;
        options.add(hsvPercentage, gbc);

        gbc.gridx = 0;
        gbc.insets.top = 20;
        JPanel row3_stampConfig = new JPanel(new GridBagLayout());
        StampJPanel row3_stampPreview = new StampJPanel();
        row3_stampPreview.setMargin(10);
        row3_stampPreview.setBackground(ImageManager.getCodePreview());
        IStamp stamp = new CubeStamp(config, null);
        row3_stampPreview.setStamp(stamp);

        final Function[] onUpdate = {null};

        String[] stampTitles = new String[StampType.values().length];
        for(int i = 0; i < stampTitles.length; i++) {
            stampTitles[i] = StampType.values()[i].getTitle();
        }
        JComboBox<Object> stampDropdown = new JComboBox<>(stampTitles);
        stampDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IStamp newStamp = StampType.getByIndex(stampDropdown.getSelectedIndex()).getIStamp(config, null);
                row3_stampPreview.setStamp(newStamp);
                setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config, onUpdate[0]);
                saveButtonUpdate[0].run();
            }
        });

        options.add(stampDropdown, gbc);
        gbc.gridx = 1;
        JPanel previewToggleAndLabel = new JPanel(new GridLayout(0,2));
        previewToggleAndLabel.add(configWindow.createJLabel(LangManager.getItem("config_label_preview"), JLabel.RIGHT, JLabel.CENTER));
        JCheckBox previewBGToggle = new JCheckBox();
        previewBGToggle.setSelected(true);
        previewBGToggle.addChangeListener(e -> row3_stampPreview.setBackgroundEnabled(previewBGToggle.isSelected()));
        previewToggleAndLabel.add(previewBGToggle);
        options.add(previewToggleAndLabel, gbc);
        gbc.gridx = 2;
        options.add(new JPanel(),gbc);
        options.add(new InfoButton(null), gbc);
        gbc.gridx = 0;
        gbc.insets.top = 0;
        options.add(row3_stampConfig, gbc);
        gbc.gridx = 1;
        options.add(row3_stampPreview, gbc);

        //END ELEMENTS

        saveButtonUpdate[0] = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true);
        onUpdate[0] = new Function() {
            @Override
            public boolean run() {
                return saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            }
        };
        setupStampConfigPanel(row3_stampConfig, stamp, row3_stampPreview, config, onUpdate[0]);


        add(options);

        if(disablePage)
            configWindow.setEnabledAll(options, false, dropdown);
    }

    @Override
    public ConfigWindow.PAGE getPage() {
        return ConfigWindow.PAGE.editorPanel;
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    private JSpinner setupStampConfigPanelSpinner(ConfigHelper.PROFILE configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex, Function onUpdate) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Double.parseDouble(config.getFloat(configKey)+""), min, max, stepSize));
        spinner.addChangeListener(e -> {
            config.set(configKey, (int)Double.parseDouble(spinner.getValue().toString()));
            previewPanel.setStamp(StampType.getByIndex(stampIndex).getIStamp(config, null));
            onUpdate.run();
        });
        return spinner;
    }

    private void setupStampConfigPanelSpinnerWithLabel(JPanel panel, String title, ConfigHelper.PROFILE configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex, GridBagConstraints constraints, String infoText, Function onUpdate) {
        constraints.gridx = 0;
        panel.add(configWindow.createJLabel(title, JLabel.RIGHT, JLabel.CENTER), constraints);
        constraints.gridx = 1;
        panel.add(setupStampConfigPanelSpinner(configKey, min, max, stepSize,previewPanel, config, stampIndex, onUpdate), constraints);
        constraints.gridx = 2;
        panel.add(new InfoButton(infoText), constraints);
        constraints.gridx = 0;
    }

    public void setupStampConfigPanel(JPanel panel, IStamp stamp, StampJPanel previewPanel, Config config, Function onUpdate) {
        panel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        if(stamp instanceof CubeStamp) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCubeDefaultColor, e -> {
                previewPanel.setStamp(new CubeStamp(config, null));
                onUpdate.run();
            }), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            panel.add(configWindow.createJLabel("Smart Pixel", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JCheckBox smartPixelCheckBox = new JCheckBox();
            smartPixelCheckBox.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCubeSmartPixel));
            smartPixelCheckBox.addActionListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCubeSmartPixel, smartPixelCheckBox.isSelected() + "");
                onUpdate.run();
            });
            panel.add(smartPixelCheckBox, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            int stampIndex = StampType.CUBE.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startwidth"), ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startheight"), ConfigHelper.PROFILE.editorStampCubeHeight, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthspeed"), ConfigHelper.PROFILE.editorStampCubeWidthSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightspeed"), ConfigHelper.PROFILE.editorStampCubeHeightSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthminimum"), ConfigHelper.PROFILE.editorStampCubeWidthMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightminimum"), ConfigHelper.PROFILE.editorStampCubeHeightMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
        } else if(stamp instanceof CounterStamp) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCounterDefaultColor, e -> previewPanel.setStamp(new CounterStamp(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            int stampIndex = StampType.COUNTER.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startwidth"), ConfigHelper.PROFILE.editorStampCounterWidth, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startheight"), ConfigHelper.PROFILE.editorStampCounterHeight, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_generalspeed"), ConfigHelper.PROFILE.editorStampCounterSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthspeed"), ConfigHelper.PROFILE.editorStampCounterWidthSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightspeed"), ConfigHelper.PROFILE.editorStampCounterHeightSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthminimum"), ConfigHelper.PROFILE.editorStampCounterWidthMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightminimum"), ConfigHelper.PROFILE.editorStampCounterHeightMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);

            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_solidcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            JCheckBox cbSolidColor = new JCheckBox();
            cbSolidColor.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor));
            cbSolidColor.addChangeListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCounterSolidColor, cbSolidColor.isSelected() + "");
                previewPanel.setStamp(new CounterStamp(config, null));
                onUpdate.run();
            });
            gbc.gridx = 1;
            panel.add(cbSolidColor, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            gbc.gridx = 0;
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_stampborder"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            JCheckBox cbBorder = new JCheckBox();
            cbBorder.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCounterBorderEnabled));
            cbBorder.addChangeListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCounterBorderEnabled, cbBorder.isSelected() + "");
                previewPanel.setStamp(new CounterStamp(config, null));
                onUpdate.run();
            });
            panel.add(cbBorder, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_fontsizemodifier"), ConfigHelper.PROFILE.editorStampCounterFontSizeModifier, 0.1, 10, 0.01D, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_bordermofizier"), ConfigHelper.PROFILE.editorStampCounterBorderModifier, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
        } else if(stamp instanceof CircleStamp) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCircleDefaultColor, e -> previewPanel.setStamp(new CircleStamp(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            int stampIndex = StampType.CIRCLE.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startwidth"), ConfigHelper.PROFILE.editorStampCircleWidth, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startheight"), ConfigHelper.PROFILE.editorStampCircleHeight, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_generalspeed"), ConfigHelper.PROFILE.editorStampCircleSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthspeed"), ConfigHelper.PROFILE.editorStampCircleWidthSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightspeed"), ConfigHelper.PROFILE.editorStampCircleHeightSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthminimum"), ConfigHelper.PROFILE.editorStampCircleWidthMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightminimum"), ConfigHelper.PROFILE.editorStampCircleHeightMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_thickness"), ConfigHelper.PROFILE.editorStampCircleThickness, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
        } else if(stamp instanceof SimpleBrush) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor, e -> previewPanel.setStamp(new SimpleBrush(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            int stampIndex = StampType.SIMPLE_BRUSH.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_brushsize"), ConfigHelper.PROFILE.editorStampSimpleBrushSize, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_brushsizespeed"), ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_linepointdistance"), ConfigHelper.PROFILE.editorStampSimpleBrushDistance, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            panel.add(new JPanel()); //Padding
        } else if(stamp instanceof TextStamp) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampTextDefaultColor, e -> previewPanel.setStamp(new TextStamp(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            int stampIndex = StampType.TEXT.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_defaultfontsize"), ConfigHelper.PROFILE.editorStampTextDefaultFontSize, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_fontsizechangespeed"), ConfigHelper.PROFILE.editorStampTextDefaultSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            for(int i = 0; i < 6; i++) panel.add(new JPanel(), gbc); //Padding
            //TODO: Draw it in the middle, possibly by giving TextStamp a getTextWidth() function and adding an edgecase to the Stamp Renderer, to move it to the left
        } else if(stamp instanceof RectangleStamp) {
            panel.add(configWindow.createJLabel(LangManager.getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampRectangleDefaultColor, e -> previewPanel.setStamp(new RectangleStamp(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            int stampIndex = StampType.RECTANGLE.getIndex();
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startwidth"), ConfigHelper.PROFILE.editorStampRectangleWidth, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_startheight"), ConfigHelper.PROFILE.editorStampRectangleHeight, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthspeed"), ConfigHelper.PROFILE.editorStampRectangleWidthSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightspeed"), ConfigHelper.PROFILE.editorStampRectangleHeightSpeed, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_widthminimum"), ConfigHelper.PROFILE.editorStampRectangleWidthMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_heightminimum"), ConfigHelper.PROFILE.editorStampRectangleHeightMinimum, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, LangManager.getItem("config_label_thickness"), ConfigHelper.PROFILE.editorStampRectangleThickness, 1, 999, 1, previewPanel, config, stampIndex, gbc, null, onUpdate);
        } else {
            panel.add(configWindow.createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER));
            for (int i = 0; i < 15; i++) panel.add(new JLabel());
        }
    }
}