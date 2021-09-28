package org.snipsniper.configwindow.tabs;

import org.snipsniper.ImageManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.configwindow.StampJPanel;
import org.snipsniper.sceditor.stamps.CubeStamp;
import org.snipsniper.sceditor.stamps.IStamp;
import org.snipsniper.sceditor.stamps.StampUtils;
import org.snipsniper.utils.Function;
import org.snipsniper.utils.InfoButton;
import org.snipsniper.utils.enums.ConfigSaveButtonState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class EditorTab extends JPanel implements ITab{
    private final ConfigWindow configWindow;

    public EditorTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {
        removeAll();

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
        options.add(configWindow.createJLabel("HSV color switch speed", JLabel.RIGHT, JLabel.CENTER), gbc);
        JLabel hsvPercentage = new JLabel(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed) + "%");
        hsvPercentage.setHorizontalAlignment(JLabel.CENTER);
        JSlider hsvSlider = new JSlider(JSlider.HORIZONTAL);
        hsvSlider.setMinimum(-100);
        hsvSlider.setMaximum(100);
        hsvSlider.setSnapToTicks(true);
        hsvSlider.addChangeListener(e -> {
            hsvPercentage.setText(hsvSlider.getValue() + "%");
            config.set(ConfigHelper.PROFILE.hsvColorSwitchSpeed, hsvSlider.getValue() + "");
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
        String theme = SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme);
        if(theme.equals("light")) {
            row3_stampPreview.setBackground(ImageManager.getImage("preview/code_light.png"));
        } else if(theme.equals("dark")) {
            row3_stampPreview.setBackground(ImageManager.getImage("preview/code_dark.png"));
        }
        IStamp stamp = new CubeStamp(config, null);
        row3_stampPreview.setStamp(stamp);

        final Function[] onUpdate = {null};

        JComboBox<Object> stampDropdown = new JComboBox<>(StampUtils.getStampsAsString());
        stampDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IStamp newStamp = StampUtils.getNewIStampByIndex(stampDropdown.getSelectedIndex(), config, null);
                row3_stampPreview.setStamp(newStamp);
                configWindow.setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config, onUpdate[0]);
                saveButtonUpdate[0].run();
            }
        });

        options.add(stampDropdown, gbc);
        gbc.gridx = 1;
        JPanel previewToggleAndLabel = new JPanel(new GridLayout(0,2));
        previewToggleAndLabel.add(configWindow.createJLabel("Preview", JLabel.RIGHT, JLabel.CENTER));
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

        saveButtonUpdate[0] = configWindow.setupSaveButtons(options, gbc, config, configOriginal, null, true);
        onUpdate[0] = new Function() {
            @Override
            public boolean run() {
                return saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            }
        };
        configWindow.setupStampConfigPanel(row3_stampConfig, stamp, row3_stampPreview, config, onUpdate[0]);


        add(options);

        if(disablePage)
            configWindow.setEnabledAll(options, false, dropdown);
    }
}