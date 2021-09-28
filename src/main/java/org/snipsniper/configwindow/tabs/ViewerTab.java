package org.snipsniper.configwindow.tabs;

import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.utils.Function;
import org.snipsniper.utils.InfoButton;
import org.snipsniper.utils.enums.ConfigSaveButtonState;

import javax.swing.*;
import java.awt.*;

public class ViewerTab extends JPanel implements ITab{
    private final ConfigWindow configWindow;
    private boolean isDirty;

    public ViewerTab(ConfigWindow configWindow) {
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
            if(configOriginal.getFilename().contains("editor"))
                disablePage = true;
        } else {
            config = new Config("disabled_cfg.cfg", "profile_defaults.cfg");
            disablePage = true;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        JPanel options = new JPanel(new GridBagLayout());

        JComponent dropdown = configWindow.setupProfileDropdown(options, this, configOriginal, config, ConfigWindow.PAGE.viewerPanel, "editor");
        //BEGIN ELEMENTS

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 10, 0, 10);

        options.add(configWindow.createJLabel("Close viewer when opening editor", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox closeViewerOnEditor = new JCheckBox();
        closeViewerOnEditor.setSelected(config.getBool(ConfigHelper.PROFILE.closeViewerOnOpenEditor));
        closeViewerOnEditor.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.closeViewerOnOpenEditor, closeViewerOnEditor.isSelected());
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(closeViewerOnEditor, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

        gbc.gridx = 0;
        options.add(configWindow.createJLabel("Open viewer in fullscreen", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox openViewerFullscreen = new JCheckBox();
        openViewerFullscreen.setSelected(config.getBool(ConfigHelper.PROFILE.openViewerInFullscreen));
        openViewerFullscreen.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.openViewerInFullscreen, openViewerFullscreen.isSelected());
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(openViewerFullscreen, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);

        //END ELEMENTS

        saveButtonUpdate[0] = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true);

        add(options);

        if(disablePage)
            configWindow.setEnabledAll(options, false, dropdown);
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }
}
