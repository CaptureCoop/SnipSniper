package org.snipsniper.configwindow.tabs;

import org.snipsniper.ImageManager;
import org.snipsniper.LangManager;
import org.snipsniper.colorchooser.ColorChooser;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.configwindow.HotKeyButton;
import org.snipsniper.configwindow.folderpreview.FolderPreview;
import org.snipsniper.configwindow.iconwindow.IconWindow;
import org.snipsniper.utils.*;
import org.snipsniper.utils.enums.ConfigSaveButtonState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class GeneralTab extends JPanel implements ITab{
    private final ConfigWindow configWindow;
    private boolean isDirty;

    public GeneralTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {
        removeAll();
        isDirty = false;

        final ColorChooser[] colorChooser = {null};

        final Function[] cleanDirtyFunction = {null};

        Config config;
        boolean disablePage = false;
        if (configOriginal != null) {
            config = new Config(configOriginal);
            if(configOriginal.getFilename().contains("viewer") || configOriginal.getFilename().contains("editor"))
                disablePage = true;
        } else {
            config = new Config("disabled_cfg.cfg", "profile_defaults.cfg");
            disablePage = true;
        }

        GridBagConstraints gbc = new GridBagConstraints();
        JPanel options = new JPanel(new GridBagLayout());

        JComboBox<DropdownItem> dropdown = configWindow.setupProfileDropdown(options, this, configOriginal, config, ConfigWindow.PAGE.generalPanel, "editor", "viewer");

        //BEGIN ELEMENTS

        //BEGIN TITLE
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_title"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JPanel titleContent = new JPanel(new GridLayout(0, 2));
        JTextField titleInput = new JTextField(config.getString(ConfigHelper.PROFILE.title));
        titleInput.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if(StringUtils.removeWhitespace(titleInput.getText()).isEmpty())
                    titleInput.setText("none");
                config.set(ConfigHelper.PROFILE.title, titleInput.getText());
            }
        });
        titleContent.add(titleInput);
        JButton titleReset = new JButton(LangManager.getItem("config_label_reset"));
        titleReset.addActionListener(e -> {
            titleInput.setText("none");
            config.set(ConfigHelper.PROFILE.title, titleInput.getText());
        });
        titleContent.add(titleReset);
        options.add(titleContent, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/title.json")), gbc);
        //END TITLE

        //BEGIN ICON
        gbc.gridx = 0;
        options.add(configWindow.createJLabel("Icon", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JPanel iconPanel = new JPanel(new GridLayout(0, 2));
        JButton iconButton = new JButton(LangManager.getItem("config_label_seticon"));
        DropdownItem item = ((DropdownItem)dropdown.getSelectedItem());
        if(item != null) {
            Icon icon = ((DropdownItem)dropdown.getSelectedItem()).getIcon();
            if(icon != null)
                iconButton.setIcon(icon);
        }
        iconButton.addActionListener(e -> configWindow.addCWindow(new IconWindow("Custom Profile Icon", configWindow, args -> {
            config.set(ConfigHelper.PROFILE.icon, args[0]); //TODO: We have duplicated code here, uuuuugh. I miss having functions in functions :(
            Image img = ImageUtils.getIconDynamically(config);
            if(img == null)
                img = ImageUtils.getDefaultIcon(configWindow.getIDFromFilename(config.getFilename()));
            iconButton.setIcon(new ImageIcon(img.getScaledInstance(16, 16, 0)));
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        })));
        iconPanel.add(iconButton);
        JButton iconReset = new JButton(LangManager.getItem("config_label_reset"));
        iconReset.addActionListener(e -> {
            //TODO: Barf, duplicated code
            config.set(ConfigHelper.PROFILE.icon, "none");
            Image img = ImageUtils.getIconDynamically(config);
            if(img == null)
                img = ImageUtils.getDefaultIcon(configWindow.getIDFromFilename(config.getFilename()));
            iconButton.setIcon(new ImageIcon(img.getScaledInstance(16, 16, 0)));
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        iconPanel.add(iconReset);
        options.add(iconPanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/icon.json")), gbc);
        //END ICON

        //BEGIN HOTKEY
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JPanel hotkeyPanel = new JPanel(new GridLayout(0, 2));
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
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        hotkeyPanel.add(hotKeyButton);
        JButton deleteHotKey = new JButton(LangManager.getItem("config_label_delete"));
        deleteHotKey.addActionListener(e -> {
            hotKeyButton.setText(LangManager.getItem("config_label_none"));
            hotKeyButton.hotkey = -1;
            config.set(ConfigHelper.PROFILE.hotkey, "NONE");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        hotkeyPanel.add(deleteHotKey);
        options.add(hotkeyPanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/hotkey.json")), gbc);
        //END HOTKEY

        //BEGIN SAVEIMAGES
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox saveToDisk = new JCheckBox();
        saveToDisk.setSelected(config.getBool(ConfigHelper.PROFILE.saveToDisk));
        saveToDisk.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.saveToDisk, saveToDisk.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        gbc.gridx = 1;
        options.add(saveToDisk, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/saveimage.json")), gbc);
        //END SAVEIMAGES

        //BEGIN COPYCLIPBOARD
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox copyToClipboard = new JCheckBox();
        copyToClipboard.setSelected(config.getBool(ConfigHelper.PROFILE.copyToClipboard));
        copyToClipboard.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.copyToClipboard, copyToClipboard.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(copyToClipboard, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/copyimage.json")), gbc);
        //END COPYCLIPBOARD

        //BEGIN BORDERSIZE
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JPanel borderSizePanel = new JPanel(new GridLayout(0, 2));
        JSpinner borderSize = new JSpinner(new SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.borderSize), 0.0, 999, 1.0)); //TODO: Extend JSpinner class to notify user of too large number
        borderSize.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.borderSize, (int)((double) borderSize.getValue()) + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        borderSizePanel.add(borderSize);

        SSColor borderColor = SSColor.fromSaveString(config.getString(ConfigHelper.PROFILE.borderColor));
        GradientJButton colorBtn = new GradientJButton("Color", borderColor);
        borderColor.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.borderColor, ((SSColor)e.getSource()).toSaveString());
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        colorBtn.addActionListener(e -> {
            if(colorChooser[0] == null || !colorChooser[0].isDisplayable()) {
                int x = (int)((getLocation().getX() + getWidth()/2));
                int y = (int)((getLocation().getY() + getHeight()/2));
                colorChooser[0] = new ColorChooser(config, LangManager.getItem("config_label_bordercolor"), borderColor, null, x, y, true);
                colorChooser[0].addWindowListener(() -> colorChooser[0] = null);
                configWindow.addCWindow(colorChooser[0]);
            }
        });
        borderSizePanel.add(colorBtn, gbc);
        options.add(borderSizePanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/bordersize.json")), gbc);
        //END BORDERSIZE

        //BEGIN LOCATION
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_picturelocation"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JTextField pictureLocation = new JTextField(config.getRawString(ConfigHelper.PROFILE.pictureFolder));
        pictureLocation.setPreferredSize(new Dimension(200, pictureLocation.getHeight()));
        pictureLocation.setMaximumSize(new Dimension(200, pictureLocation.getHeight()));
        pictureLocation.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                String saveLocationRaw = pictureLocation.getText();
                if(!saveLocationRaw.endsWith("/"))
                    saveLocationRaw += "/";

                String saveLocationFinal = StringUtils.replaceVars(saveLocationRaw);

                File saveLocationCheck = new File(saveLocationFinal);
                if(!saveLocationCheck.exists()) {
                    cleanDirtyFunction[0].run(ConfigSaveButtonState.NO_SAVE);
                    int dialogResult = Utils.showPopup(configWindow, LangManager.getItem("config_sanitation_directory_notexist") + " Create?", LangManager.getItem("config_sanitation_error"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ImageManager.getImage("icons/folder.png"), true);
                    if(dialogResult == JOptionPane.YES_OPTION) {
                        boolean allow = new File(saveLocationFinal).mkdirs();

                        if(!allow) {
                            configWindow.msgError(LangManager.getItem("config_sanitation_failed_createdirectory"));
                            cleanDirtyFunction[0].run(ConfigSaveButtonState.NO_SAVE);
                        } else {
                            config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationRaw);
                            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
                            cleanDirtyFunction[0].run(ConfigSaveButtonState.YES_SAVE);
                        }
                    } else {
                        if(configOriginal != null)
                            pictureLocation.setText(configOriginal.getRawString(ConfigHelper.PROFILE.pictureFolder));
                    }
                } else {
                    cleanDirtyFunction[0].run(ConfigSaveButtonState.YES_SAVE);
                    config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationFinal);
                }
            }
        });
        options.add(pictureLocation, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/imagefolder.json")), gbc);
        //END LOCATION

        //BEGIN CUSTOM MODIFIER
        gbc.gridx = 0;
        options.add(configWindow.createJLabel("Save folder modifier", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JButton customSaveButton = new JButton(StringUtils.formatDateArguments(config.getString(ConfigHelper.PROFILE.saveFolderCustom)));
        customSaveButton.addActionListener(e -> {
            FolderPreview preview = new FolderPreview("Custom save folder modifier", config.getString(ConfigHelper.PROFILE.saveFolderCustom));
            configWindow.addCWindow(preview);
            int x = (int) (getLocation().getX() + getWidth() / 2) - preview.getWidth() / 2;
            int y = (int) (getLocation().getY() + getHeight() / 2) - preview.getHeight() / 2;
            preview.setLocation(x, y);
            preview.setOnSave(args -> {
                String text = preview.getText();
                if(text.isEmpty())
                    text = "/";
                config.set(ConfigHelper.PROFILE.saveFolderCustom, text);
                customSaveButton.setText(StringUtils.formatDateArguments(text));
                cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            });
        });
        options.add(customSaveButton, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/savefoldermodifier.json")), gbc);
        //END CUSTOM MODIFIER

        //BEGIN SNIPE DELAY
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JSpinner snipeDelay = new JSpinner(new SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.snipeDelay), 0.0, 100, 1.0));
        snipeDelay.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.snipeDelay, (int)((double) snipeDelay.getValue()) + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(snipeDelay, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/snapdelay.json")), gbc);
        //END SNIPE DELAY

        //BEGIN OPEN EDITOR
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox openEditor = new JCheckBox();
        openEditor.setSelected(config.getBool(ConfigHelper.PROFILE.openEditor));
        openEditor.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.openEditor, openEditor.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(openEditor, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/openeditor.json")), gbc);
        //END OPEN EDITOR

        //BEGIN SPYGLASS
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_spyglass"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;

        JComboBox<Object> spyglassDropdownEnabled = new JComboBox<>(new String[]{LangManager.getItem("config_label_disabled"), LangManager.getItem("config_label_enabled"), LangManager.getItem("config_label_hold"), LangManager.getItem("config_label_toggle")});
        JComboBox<Object> spyglassDropdownHotkey = new JComboBox<>(new String[]{LangManager.getItem("config_label_control"), LangManager.getItem("config_label_shift")});
        String startMode = config.getString(ConfigHelper.PROFILE.spyglassMode);
        boolean startEnabled = config.getBool(ConfigHelper.PROFILE.enableSpyglass);
        spyglassDropdownHotkey.setVisible(false);
        if(!startMode.equals("none") && startEnabled) {
            switch(startMode) {
                case "hold":
                    spyglassDropdownEnabled.setSelectedIndex(2);
                    break;
                case "toggle":
                    spyglassDropdownEnabled.setSelectedIndex(3);
                    break;
            }
            spyglassDropdownHotkey.setVisible(true);
        } else if(startMode.equals("none") && startEnabled){
            spyglassDropdownEnabled.setSelectedIndex(1);
        } else {
            spyglassDropdownEnabled.setSelectedIndex(0);
        }

        switch(config.getInt(ConfigHelper.PROFILE.spyglassHotkey)) {
            case KeyEvent.VK_CONTROL: spyglassDropdownHotkey.setSelectedIndex(0); break;
            case KeyEvent.VK_SHIFT: spyglassDropdownHotkey.setSelectedIndex(1); break;
        }
        spyglassDropdownEnabled.addItemListener(e -> {
            boolean enableSpyglass;
            String spyglassMode;
            switch(spyglassDropdownEnabled.getSelectedIndex()) {
                case 0:
                    spyglassDropdownHotkey.setVisible(false);
                    enableSpyglass = false; spyglassMode = "none";
                    break;
                case 1:
                    spyglassDropdownHotkey.setVisible(false);
                    enableSpyglass = true; spyglassMode = "none";
                    break;
                case 2:
                    spyglassDropdownHotkey.setVisible(true);
                    enableSpyglass = true; spyglassMode = "hold";
                    break;
                case 3:
                    spyglassDropdownHotkey.setVisible(true);
                    enableSpyglass = true; spyglassMode = "toggle";
                    break;
                default:
                    enableSpyglass = false; spyglassMode = "none";
            }
            config.set(ConfigHelper.PROFILE.enableSpyglass, enableSpyglass);
            config.set(ConfigHelper.PROFILE.spyglassMode, spyglassMode);
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        spyglassDropdownHotkey.addItemListener(e -> {
            switch(spyglassDropdownHotkey.getSelectedIndex()) {
                case 0: config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_CONTROL); break;
                case 1: config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_SHIFT); break;
            }
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });

        JPanel spyglassPanel = new JPanel(configWindow.getGridLayoutWithMargin(0, 2, 0));
        spyglassPanel.add(spyglassDropdownEnabled);
        spyglassPanel.add(spyglassDropdownHotkey);
        options.add(spyglassPanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/usespyglass.json")), gbc);
        //END SPYGLASS

        //BEGIN SPYGLASS ZOOM
        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_spyglasszoom"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JComboBox<Object> spyglassZoomDropdown = new JComboBox<>(new String[]{"8x8", "16x16", "32x32", "64x64"});
        switch(config.getInt(ConfigHelper.PROFILE.spyglassZoom)) {
            case 8: spyglassZoomDropdown.setSelectedIndex(0); break;
            case 16: spyglassZoomDropdown.setSelectedIndex(1); break;
            case 32: spyglassZoomDropdown.setSelectedIndex(2); break;
            case 64: spyglassZoomDropdown.setSelectedIndex(3); break;
        }

        spyglassZoomDropdown.addItemListener(e -> {
            int zoom = 16;
            switch(spyglassZoomDropdown.getSelectedIndex()) {
                case 0: zoom = 8; break;
                case 1: zoom = 16; break;
                case 2: zoom = 32; break;
                case 3: zoom = 64; break;
            }
            config.set(ConfigHelper.PROFILE.spyglassZoom, zoom);
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(spyglassZoomDropdown, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(WikiManager.getContent("config/general/spyglasszoom.json")), gbc);
        //END SPYGLASS ZOOM
        //END ELEMENTS

        //BEGIN SAVE
        cleanDirtyFunction[0] = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true);
        //END SAVE

        add(options);

        if(disablePage)
            configWindow.setEnabledAll(options, false, dropdown);
    }

    @Override
    public ConfigWindow.PAGE getPage() {
        return ConfigWindow.PAGE.generalPanel;
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