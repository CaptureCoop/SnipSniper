package org.snipsniper.configwindow;

import org.apache.commons.lang3.SystemUtils;
import org.snipsniper.LangManager;
import org.snipsniper.LogManager;
import org.snipsniper.config.Config;
import org.snipsniper.SnipSniper;
import org.snipsniper.colorchooser.ColorChooser;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.folderpreview.FolderPreview;
import org.snipsniper.configwindow.iconwindow.IconWindow;
import org.snipsniper.systray.Sniper;
import org.snipsniper.sceditor.stamps.*;
import org.snipsniper.utils.*;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ConfigWindow extends JFrame implements IClosable{
    private final ConfigWindow instance;

    private JTabbedPane tabPane;
    private JPanel snipConfigPanel;
    private JPanel editorConfigPanel;
    private JPanel viewerConfigPanel;
    private JPanel globalConfigPanel;

    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();
    private final ArrayList<File> configFiles = new ArrayList<>();
    private Config lastSelectedConfig;

    public enum PAGE {snipPanel, editorPanel, viewerPanel, globalPanel}

    private final int indexSnip = 0;
    private final int indexEditor = 1;
    private final int indexViewer = 2;
    private final int indexGlobal = 3;

    private final ArrayList<IClosable> cWindows = new ArrayList<>();

    public ConfigWindow(Config config, PAGE page) {
        instance = this;
        LogManager.log("Creating config window", LogLevel.INFO);

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(Icons.getImage("icons/config.png"));
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
        File cfgFolder = new File(SnipSniper.getConfigFolder());
        File[] files = cfgFolder.listFiles();
        if(files != null) {
            for (File file : files) {
                if (FileUtils.getFileExtension(file).equals(Config.DOT_EXTENSION))
                    configFiles.add(file);
            }
        }
    }

    public void setup(Config config, PAGE page) {
        tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        final int iconSize = 16;
        int index = 0;
        int enableIndex = index;

        lastSelectedConfig = config;

        snipConfigPanel = new JPanel();
        tabPane.addTab("SnipSniper",  setupSnipPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.getImage("icons/snipsniper.png").getScaledInstance(iconSize, iconSize, 0)));
        index++;

        editorConfigPanel = new JPanel();
        tabPane.addTab("Editor", setupEditorPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.getImage("icons/editor.png").getScaledInstance(iconSize,iconSize,0)));
        if(page == PAGE.editorPanel)
            enableIndex = index;
        index++;

        viewerConfigPanel = new JPanel();
        tabPane.addTab("Viewer", setupViewerPane(config));
        tabPane.setIconAt(index, new ImageIcon(Icons.getImage("icons/viewer.png").getScaledInstance(iconSize,iconSize,0)));
        if(page == PAGE.viewerPanel)
            enableIndex = index;
        index++;

        globalConfigPanel = new JPanel();
        tabPane.addTab("Global", setupGlobalPane());
        tabPane.setIconAt(index, new ImageIcon(Icons.getImage("icons/config.png").getScaledInstance(iconSize, iconSize, 0)));
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

    public JComboBox<DropdownItem> setupProfileDropdown(JPanel panelToAdd, JPanel parentPanel, Config configOriginal, Config config, PAGE page, int pageIndex, String... blacklist) {
        //Returns the dropdown, however dont add it manually
        //TODO: Refresh other dropdowns when creating new profile?
        ArrayList<DropdownItem> profiles = new ArrayList<>();
        for(File file : configFiles) {
            if(file.getName().contains("viewer")) {
                boolean add = true;
                for(String str : blacklist)
                    if (str.contains("viewer")) {
                        add = false;
                        break;
                    }
                if(add)
                    profiles.add(0, new DropdownItem("Standalone Viewer", file.getName(), Icons.getImage("icons/viewer.png")));
            } else if(file.getName().contains("editor")) {
                boolean add = true;
                for(String str : blacklist)
                    if (str.contains("editor")) {
                        add = false;
                        break;
                    }
                if(add)
                    profiles.add(0, new DropdownItem("Standalone Editor", file.getName(), Icons.getImage("icons/editor.png")));
            } else if(file.getName().contains("profile")) {
                int nr = getIDFromFilename(file.getName());
                Image img = Utils.getIconDynamically(new Config(file.getName(), "profile_defaults.cfg"));
                if(img == null)
                    img = Utils.getDefaultIcon(nr);
                profiles.add(new DropdownItem("Profile " + nr, file.getName(), img));
            }
        }

        if(configOriginal == null)
            profiles.add(0, new DropdownItem("Select a profile", "select_profile"));

        DropdownItem[] items = new DropdownItem[profiles.size()];
        for(int i = 0; i < profiles.size(); i++)
            items[i] = profiles.get(i);

        JComboBox<DropdownItem> dropdown = new JComboBox<>(items);
        dropdown.setRenderer(new DropdownItemRenderer(items));
        if(configOriginal == null)
            dropdown.setSelectedIndex(0);
        else
            DropdownItem.setSelected(dropdown, config.getFilename());
        dropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                parentPanel.removeAll();
                Config newConfig = new Config(((DropdownItem)e.getItem()).getID(), "profile_defaults.cfg");
                tabPane.setComponentAt(pageIndex, setupPaneDynamic(newConfig, page));
                lastSelectedConfig = newConfig;
            }
        });
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
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

                    tabPane.setComponentAt(indexSnip, setupSnipPane(newProfileConfig));
                    tabPane.setComponentAt(indexEditor, setupEditorPane(newProfileConfig));
                    tabPane.setComponentAt(indexViewer, setupViewerPane(newProfileConfig));

                    lastSelectedConfig = newProfileConfig;

                    break;
                }
            }
        });
        profilePlusMinus.add(profileAddButton);
        JButton profileRemoveButton = new JButton("-");
        DropdownItem selectedItem = (DropdownItem) dropdown.getSelectedItem();
        if(selectedItem.getID().contains("profile0") || selectedItem.getID().contains("editor"))
            profileRemoveButton.setEnabled(false);
        profileRemoveButton.addActionListener(actionEvent -> {
            DropdownItem item = (DropdownItem) dropdown.getSelectedItem();
            if(!item.getID().contains("profile0") || !item.getID().contains("editor")) {
                config.deleteFile();
                SnipSniper.resetProfiles();
                refreshConfigFiles();
                parentPanel.removeAll();
                int newIndex = dropdown.getSelectedIndex() - 1;
                if(newIndex < 0)
                    newIndex = dropdown.getSelectedIndex() + 1;
                Config newConfig = new Config(dropdown.getItemAt(newIndex).getID(), "profile_defaults.cfg");

                tabPane.setComponentAt(indexSnip, setupSnipPane(newConfig));
                tabPane.setComponentAt(indexEditor, setupEditorPane(newConfig));
                tabPane.setComponentAt(indexViewer, setupViewerPane(newConfig));

                lastSelectedConfig = newConfig;
            }
        });
        profilePlusMinus.add(profileRemoveButton);
        panelToAdd.add(profilePlusMinus, gbc);
        return dropdown;
    }

    //Returns function you can run to update the state
    private Function setupSaveButtons(JPanel panel, GridBagConstraints gbc, Config config, Config configOriginal) {
        final boolean[] allowSaving = {true};
        final boolean[] isDirty = {false};
        JButton save = new JButton(LangManager.getItem("config_label_save"));
        save.addActionListener(e -> {
            if(allowSaving[0] && configOriginal != null) {
                configOriginal.loadFromConfig(config);
                configOriginal.save();
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();

                SnipSniper.resetProfiles();
                tabPane.setComponentAt(indexSnip, setupSnipPane(configOriginal));
                tabPane.setComponentAt(indexEditor, setupEditorPane(configOriginal));
                tabPane.setComponentAt(indexViewer, setupViewerPane(configOriginal));
            }
        });

        JButton close = new JButton("Close");
        close.addActionListener(e -> {
            if(isDirty[0]) {
                int result = JOptionPane.showConfirmDialog(instance, "Unsaved changes, are you sure you want to cancel?","Warning", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            close();
        });
        Function setState = new Function() {
            @Override
            public boolean run(ConfigSaveButtonState state) {
                switch (state) {
                    case UPDATE_CLEAN_STATE: isDirty[0] = !config.equals(configOriginal); break;
                    case YES_SAVE: allowSaving[0] = true; break;
                    case NO_SAVE: allowSaving[0] = false; break;
                }
                if(isDirty[0])
                    close.setText("Cancel");
                else
                    close.setText("Close");

                return true;
            }
        };
        gbc.insets.top = 20;
        gbc.gridx = 0;
        panel.add(save, gbc);
        gbc.gridx = 1;
        panel.add(close, gbc);
        return setState;
    }

    private int getIDFromFilename(String name) {
        String idString = name.replaceAll(Config.DOT_EXTENSION, "").replace("profile", "");
        if(MathUtils.isInteger(idString)) {
            return Integer.parseInt(idString);
        }
        LogManager.log("Issie parsing Filename to id: " + name, LogLevel.ERROR);
        return -1;
    }

    public JComponent setupSnipPane(Config configOriginal) {
        snipConfigPanel.removeAll();

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

        JComboBox<DropdownItem> dropdown = setupProfileDropdown(options, snipConfigPanel, configOriginal, config, PAGE.snipPanel, indexSnip, "editor", "viewer");

        //BEGIN ELEMENTS

        //BEGIN ICON
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 10, 0, 10);
        options.add(createJLabel("Icon", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JButton iconButton = new JButton("Set Icon");
        Icon icon = ((DropdownItem)dropdown.getSelectedItem()).getIcon();
        if(icon != null)
            iconButton.setIcon(icon);
        iconButton.addActionListener(e -> cWindows.add(new IconWindow("Custom Profile Icon", instance, args -> {
            config.set(ConfigHelper.PROFILE.icon, args[0]);
            Image img = Utils.getIconDynamically(config);
            if(img == null)
                img = Utils.getDefaultIcon(getIDFromFilename(config.getFilename()));
            iconButton.setIcon(new ImageIcon(img.getScaledInstance(16, 16, 0)));
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        })));
        options.add(iconButton, gbc);
        //END ICON

        //BEGIN HOTKEY
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_hotkey"), JLabel.RIGHT, JLabel.CENTER), gbc);
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
        options.add(new InfoButton(null), gbc);
        //END HOTKEY

        //BEGIN SAVEIMAGES
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_saveimages"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox saveToDisk = new JCheckBox();
        saveToDisk.setSelected(config.getBool(ConfigHelper.PROFILE.saveToDisk));
        saveToDisk.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.saveToDisk, saveToDisk.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        gbc.gridx = 1;
        options.add(saveToDisk, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END SAVEIMAGES

        //BEGIN COPYCLIPBOARD
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_copyclipboard"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox copyToClipboard = new JCheckBox();
        copyToClipboard.setSelected(config.getBool(ConfigHelper.PROFILE.copyToClipboard));
        copyToClipboard.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.copyToClipboard, copyToClipboard.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(copyToClipboard, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END COPYCLIPBOARD

        //BEGIN BORDERSIZE
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_bordersize"), JLabel.RIGHT, JLabel.CENTER), gbc);
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
                cWindows.add(colorChooser[0]);
            }
        });
        borderSizePanel.add(colorBtn, gbc);
        options.add(borderSizePanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END BORDERSIZE

        //BEGIN LOCATION
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_picturelocation"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JTextField pictureLocation = new JTextField(config.getRawString(ConfigHelper.PROFILE.pictureFolder));
        pictureLocation.setPreferredSize(new Dimension(200, pictureLocation.getHeight()));
        pictureLocation.setMaximumSize(new Dimension(200, pictureLocation.getHeight()));
        pictureLocation.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent focusEvent) {
                String saveLocationFinal = pictureLocation.getText();
                if(!saveLocationFinal.endsWith("/"))
                    saveLocationFinal += "/";

                saveLocationFinal = StringUtils.replaceVars(saveLocationFinal);

                File saveLocationCheck = new File(saveLocationFinal);
                if(!saveLocationCheck.exists()) {
                    boolean allow = false;
                    Object[] options = {"Okay" , LangManager.getItem("config_sanitation_createdirectory") };
                    int msgBox = JOptionPane.showOptionDialog(null,LangManager.getItem("config_sanitation_directory_notexist"), LangManager.getItem("config_sanitation_error"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                    if(msgBox == 1) {
                        allow = new File(saveLocationFinal).mkdirs();

                        if(!allow) {
                            msgError(LangManager.getItem("config_sanitation_failed_createdirectory"));
                        } else {
                            config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationFinal);
                        }
                    }
                    if(allow)
                        cleanDirtyFunction[0].run(ConfigSaveButtonState.YES_SAVE);
                    else
                        cleanDirtyFunction[0].run(ConfigSaveButtonState.NO_SAVE);
                } else {
                    cleanDirtyFunction[0].run(ConfigSaveButtonState.YES_SAVE);
                    config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationFinal);
                }
            }
        });
        options.add(pictureLocation, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END LOCATION

        //BEGIN CUSTOM MODIFIER
        gbc.gridx = 0;
        options.add(createJLabel("Save folder modifier", JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JButton customSaveButton = new JButton(StringUtils.formatDateArguments(config.getString(ConfigHelper.PROFILE.saveFolderCustom)));
        customSaveButton.addActionListener(e -> {
            FolderPreview preview = new FolderPreview("Custom save folder modifier", config.getString(ConfigHelper.PROFILE.saveFolderCustom));
            cWindows.add(preview);
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
        options.add(new InfoButton(null), gbc);
        //END CUSTOM MODIFIER

        //BEGIN SNIPE DELAY
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_snapdelay"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JSpinner snipeDelay = new JSpinner(new SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.snipeDelay), 0.0, 100, 1.0));
        snipeDelay.addChangeListener(e -> {
            config.set(ConfigHelper.PROFILE.snipeDelay, (int)((double) snipeDelay.getValue()) + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(snipeDelay, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END SNIPE DELAY

        //BEGIN OPEN EDITOR
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_openeditor"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        JCheckBox openEditor = new JCheckBox();
        openEditor.setSelected(config.getBool(ConfigHelper.PROFILE.openEditor));
        openEditor.addActionListener(e -> {
            config.set(ConfigHelper.PROFILE.openEditor, openEditor.isSelected() + "");
            cleanDirtyFunction[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        options.add(openEditor, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END OPEN EDITOR

        //BEGIN SPYGLASS
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_spyglass"), JLabel.RIGHT, JLabel.CENTER), gbc);
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

        JPanel spyglassPanel = new JPanel(getGridLayoutWithMargin(0, 2, 0));
        spyglassPanel.add(spyglassDropdownEnabled);
        spyglassPanel.add(spyglassDropdownHotkey);
        options.add(spyglassPanel, gbc);
        gbc.gridx = 2;
        options.add(new InfoButton(null), gbc);
        //END SPYGLASS

        //BEGIN SPYGLASS ZOOM
        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_spyglasszoom"), JLabel.RIGHT, JLabel.CENTER), gbc);
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
        options.add(new InfoButton(null), gbc);
        //END SPYGLASS ZOOM
        //END ELEMENTS

        //BEGIN SAVE
        cleanDirtyFunction[0] = setupSaveButtons(options, gbc, config, configOriginal);
        //END SAVE

        snipConfigPanel.add(options);

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(snipConfigPanel);
    }

    public JComponent setupEditorPane(Config configOriginal) {
        editorConfigPanel.removeAll();

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

        JComponent dropdown = setupProfileDropdown(options, editorConfigPanel, configOriginal, config, PAGE.editorPanel, indexEditor, "viewer");
        //BEGIN ELEMENTS

        gbc.gridx = 0;
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
            row3_stampPreview.setBackground(Icons.getImage("preview/code_light.png"));
        } else if(theme.equals("dark")) {
            row3_stampPreview.setBackground(Icons.getImage("preview/code_dark.png"));
        }
        IStamp stamp = new CubeStamp(config, null);
        row3_stampPreview.setStamp(stamp);

        final Function[] onUpdate = {null};

        JComboBox<Object> stampDropdown = new JComboBox<>(StampUtils.getStampsAsString());
        stampDropdown.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                IStamp newStamp = StampUtils.getNewIStampByIndex(stampDropdown.getSelectedIndex(), config, null);
                row3_stampPreview.setStamp(newStamp);
                setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config, onUpdate[0]);
                saveButtonUpdate[0].run();
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
        options.add(new JPanel(),gbc);
        options.add(new InfoButton(null), gbc);
        gbc.gridx = 0;
        gbc.insets.top = 0;
        options.add(row3_stampConfig, gbc);
        gbc.gridx = 1;
        options.add(row3_stampPreview, gbc);

        //END ELEMENTS

        saveButtonUpdate[0] = setupSaveButtons(options, gbc, config, configOriginal);
        onUpdate[0] = new Function() {
            @Override
            public boolean run() {
                return saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            }
        };
        setupStampConfigPanel(row3_stampConfig, stamp, row3_stampPreview, config, onUpdate[0]);


        editorConfigPanel.add(options);

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(editorConfigPanel);
    }

    private JSpinner setupStampConfigPanelSpinner(Enum configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex, Function onUpdate) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Double.parseDouble(config.getFloat(configKey)+""), min, max, stepSize));
        spinner.addChangeListener(e -> {
            config.set(configKey, (int)Double.parseDouble(spinner.getValue().toString()));
            previewPanel.setStamp(StampUtils.getNewIStampByIndex(stampIndex, config, null));
            onUpdate.run();
        });
        return spinner;
    }

    private void setupStampConfigPanelSpinnerWithLabel(JPanel panel, String title, Enum configKey, double min, double max, double stepSize, StampJPanel previewPanel, Config config, int stampIndex, GridBagConstraints constraints, String infoText, Function onUpdate) {
        constraints.gridx = 0;
        panel.add(createJLabel(title, JLabel.RIGHT, JLabel.CENTER), constraints);
        constraints.gridx = 1;
        panel.add(setupStampConfigPanelSpinner(configKey, min, max, stepSize,previewPanel, config, stampIndex, onUpdate), constraints);
        constraints.gridx = 2;
        panel.add(new InfoButton(infoText), constraints);
        constraints.gridx = 0;
    }

    private GradientJButton setupColorButton(String title, Config config, Enum configKey, ChangeListener whenChange) {
        SSColor startColorPBR = SSColor.fromSaveString(config.getString(configKey));
        GradientJButton colorButton = new GradientJButton(title, startColorPBR);
        startColorPBR.addChangeListener(e -> config.set(configKey, startColorPBR.toSaveString()));
        startColorPBR.addChangeListener(whenChange);
        colorButton.addActionListener(e -> cWindows.add(new ColorChooser(config, "Stamp color", startColorPBR, null, (int) (getLocation().getX() + getWidth() / 2), (int) (getLocation().getY() + getHeight() / 2), true)));
        return colorButton;
    }

    private void setupStampConfigPanel(JPanel panel, IStamp stamp, StampJPanel previewPanel, Config config, Function onUpdate) {
        panel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        if(stamp instanceof CubeStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCubeDefaultColor, e -> {
                previewPanel.setStamp(new CubeStamp(config, null));
                onUpdate.run();
            }), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            panel.add(createJLabel("Smart Pixel", JLabel.RIGHT, JLabel.CENTER), gbc);
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

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCubeWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCubeHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCubeWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCubeHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCubeWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCubeHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CUBE, gbc, null, onUpdate);
        } else if(stamp instanceof CounterStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCounterDefaultColor, e -> previewPanel.setStamp(new CounterStamp(config))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);
            gbc.gridx = 0;

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCounterWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCounterHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "General change speed", ConfigHelper.PROFILE.editorStampCounterSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCounterWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCounterHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCounterWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCounterHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);

            panel.add(createJLabel("Solid color", JLabel.RIGHT, JLabel.CENTER), gbc);
            JCheckBox cbSolidColor = new JCheckBox();
            cbSolidColor.setSelected(config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor));
            cbSolidColor.addChangeListener(e -> {
                config.set(ConfigHelper.PROFILE.editorStampCounterSolidColor, cbSolidColor.isSelected() + "");
                previewPanel.setStamp(new CounterStamp(config));
                onUpdate.run();
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
                onUpdate.run();
            });
            panel.add(cbBorder, gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Font size modifier", ConfigHelper.PROFILE.editorStampCounterFontSizeModifier, 0.1, 10, 0.01D, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Border modifier", ConfigHelper.PROFILE.editorStampCounterBorderModifier, 1, 999, 1, previewPanel, config, StampUtils.INDEX_COUNTER, gbc, null, onUpdate);
        } else if(stamp instanceof CircleStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCircleDefaultColor, e -> previewPanel.setStamp(new CircleStamp(config))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton(null), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampCircleWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampCircleHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "General change speed", ConfigHelper.PROFILE.editorStampCircleSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampCircleWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampCircleHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampCircleWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampCircleHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Thickness", ConfigHelper.PROFILE.editorStampCircleThickness, 1, 999, 1, previewPanel, config, StampUtils.INDEX_CIRCLE, gbc, null, onUpdate);
        } else if(stamp instanceof SimpleBrush) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor, e -> previewPanel.setStamp(new SimpleBrush(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Brush size", ConfigHelper.PROFILE.editorStampSimpleBrushSize, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Brush size change speed", ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null, onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Line point distance", ConfigHelper.PROFILE.editorStampSimpleBrushDistance, 1, 999, 1, previewPanel, config, StampUtils.INDEX_SIMPLE_BRUSH, gbc, null, onUpdate);
            panel.add(new JPanel()); //Padding
        } else if(stamp instanceof TextStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampTextDefaultColor, e -> previewPanel.setStamp(new TextStamp(config, null))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Default font size", ConfigHelper.PROFILE.editorStampTextDefaultFontSize, 1, 999, 1, previewPanel, config, StampUtils.INDEX_TEXT, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Font size change speed", ConfigHelper.PROFILE.editorStampTextDefaultSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_TEXT, gbc, "text", onUpdate);
            for(int i = 0; i < 6; i++) panel.add(new JPanel(), gbc); //Padding
            //TODO: Draw it in the middle, possibly by giving TextStamp a getTextWidth() function and adding an edgecase to the Stamp Renderer, to move it to the left
        } else if(stamp instanceof RectangleStamp) {
            panel.add(createJLabel("Start color", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;
            panel.add(setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampRectangleDefaultColor, e -> previewPanel.setStamp(new RectangleStamp(config))), gbc);
            gbc.gridx = 2;
            panel.add(new InfoButton("text"), gbc);

            setupStampConfigPanelSpinnerWithLabel(panel, "Start width", ConfigHelper.PROFILE.editorStampRectangleWidth, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Start height", ConfigHelper.PROFILE.editorStampRectangleHeight, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Width change speed", ConfigHelper.PROFILE.editorStampRectangleWidthSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Height change speed", ConfigHelper.PROFILE.editorStampRectangleHeightSpeed, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum width", ConfigHelper.PROFILE.editorStampRectangleWidthMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Minimum height", ConfigHelper.PROFILE.editorStampRectangleHeightMinimum, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
            setupStampConfigPanelSpinnerWithLabel(panel, "Thickness", ConfigHelper.PROFILE.editorStampRectangleThickness, 1, 999, 1, previewPanel, config, StampUtils.INDEX_RECTANGLE, gbc, "text", onUpdate);
        } else {
            panel.add(createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER));
            for (int i = 0; i < 15; i++) panel.add(new JLabel());
        }
    }

    public JComponent setupViewerPane(Config configOriginal) {
        viewerConfigPanel.removeAll();

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

        JComponent dropdown = setupProfileDropdown(options, viewerConfigPanel, configOriginal, config, PAGE.viewerPanel, indexViewer, "editor");
        //BEGIN ELEMENTS

        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 10, 0, 10);

        options.add(createJLabel("Close viewer when opening editor", JLabel.RIGHT, JLabel.CENTER), gbc);
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
        options.add(createJLabel("Open viewer in fullscreen", JLabel.RIGHT, JLabel.CENTER), gbc);
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

        saveButtonUpdate[0] = setupSaveButtons(options, gbc, config, configOriginal);

        viewerConfigPanel.add(options);

        if(disablePage)
            setEnabledAll(options, false, dropdown);

        return generateScrollPane(viewerConfigPanel);
    }

    public JComponent setupGlobalPane() {
        globalConfigPanel.removeAll();

        JPanel options = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.insets.bottom = 20;

        Config config = new Config(SnipSniper.getConfig());

        JButton importConfigs = new JButton("Import Configs");
        importConfigs.addActionListener(e -> {
            int dialogResult = JOptionPane.showConfirmDialog (instance, "This will overwrite all current configs. Do you want to continue?","Warning", JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.NO_OPTION){
                return;
            }

            File imgFolder = new File(SnipSniper.getImageFolder());
            File cfgFolder = new File(SnipSniper.getConfigFolder());
            FileUtils.delete(imgFolder); FileUtils.mkdirs(imgFolder);
            FileUtils.delete(cfgFolder); FileUtils.mkdirs(cfgFolder);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP File", "zip"));
            int option = fileChooser.showOpenDialog(instance);
            if(option == JFileChooser.APPROVE_OPTION) {
                try {
                    byte[] buffer = new byte[4096];
                    FileInputStream fis = new FileInputStream(fileChooser.getSelectedFile());
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ZipInputStream zis = new ZipInputStream(bis);
                    ZipEntry ze;

                    while ((ze = zis.getNextEntry()) != null) {
                        Path filePath = Paths.get(SnipSniper.getMainFolder()).resolve(ze.getName());
                        try (FileOutputStream fos = new FileOutputStream(filePath.toFile());
                            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                bos.write(buffer, 0, len);
                            }
                        }
                    }
                    fis.close();
                    bis.close();
                    zis.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            refreshConfigFiles();
            SnipSniper.refreshGlobalConfigFromDisk();
            SnipSniper.refreshTheme();
            SnipSniper.resetProfiles();
            new ConfigWindow(null, PAGE.globalPanel);
            close();
        });
        JButton exportButton = new JButton("Export Configs");
        exportButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("ZIP File","zip"));
            chooser.setSelectedFile(new File("configs.zip"));
            int option = chooser.showSaveDialog(instance);
            if (option == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                if(!path.endsWith(".zip")) path += ".zip";
                File zip = new File(path);
                String mainFolder = SnipSniper.getMainFolder();
                ArrayList<String> files = FileUtils.getFilesInFolders(mainFolder);
                try {
                    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zip));
                    for(String file : files) {
                        if(!file.contains("logs")) {
                            String filename = file.replace(mainFolder, "");
                            if(filename.startsWith("/"))
                                filename = filename.substring(1);
                            ZipEntry zipEntry = new ZipEntry(filename);
                            out.putNextEntry(zipEntry);
                            Files.copy(new File(file).toPath(), out);
                            out.closeEntry();
                        }
                    }
                    out.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        options.add(importConfigs, gbc);
        gbc.gridx = 1;
        options.add(exportButton, gbc);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 10, 0, 10);
        String version = SnipSniper.getVersion().getDigits();
        ReleaseType releaseType = Utils.getReleaseType(SnipSniper.getConfig().getString(ConfigHelper.MAIN.updateChannel));
        if(releaseType == ReleaseType.DEV)
            version = SnipSniper.getVersion().getGithash();
        String channel = releaseType.toString().toLowerCase();
        options.add(createJLabel(StringUtils.format("Current Version: %c (%c)", version, channel), JLabel.CENTER, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(new UpdateButton(), gbc);

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

        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_language"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(languageDropdown, gbc);

        String[] themes = {LangManager.getItem("config_label_theme_light"), LangManager.getItem("config_label_theme_dark")};
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

        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_theme"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(themeDropdown, gbc);

        gbc.gridx = 0;
        options.add(createJLabel(LangManager.getItem("config_label_debug"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox debugCheckBox = new JCheckBox();
        debugCheckBox.setSelected(config.getBool(ConfigHelper.MAIN.debug));
        debugCheckBox.addActionListener(e -> config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected() + ""));
        gbc.gridx = 1;
        options.add(debugCheckBox, gbc);


        if(SystemUtils.IS_OS_WINDOWS && SnipSniper.getVersion().getPlatformType() == PlatformType.JAR) {
            gbc.gridx = 0;
            options.add(createJLabel("Start with Windows", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;

            final String userHome = System.getProperty("user.home");
            final String startup = userHome + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/SnipSniper/";
            final String batchMain = "SnipSniper.bat";
            final String linkMain = "SnipSniper.lnk";
            final String icoMain = "SnipSniper.ico";

            JCheckBox autostartCheckbox = new JCheckBox();
            autostartCheckbox.setSelected(FileUtils.exists(startup + linkMain));

            autostartCheckbox.addActionListener(e -> {
                if(autostartCheckbox.isSelected()) {
                    FileUtils.mkdirs(startup);
                    FileUtils.copyFromJar("org/snipsniper/resources/batch/" + batchMain, SnipSniper.getJarFolder() + "/" + batchMain);
                    FileUtils.copyFromJar("org/snipsniper/resources/img/icons/" + icoMain.toLowerCase(), SnipSniper.getJarFolder() + "/" + icoMain);
                    ShellLinkUtils.createShellLink(startup + linkMain, SnipSniper.getJarFolder() + batchMain, SnipSniper.getJarFolder() + "/" + icoMain);
                } else {
                    FileUtils.deleteRecursively(startup);
                }
            });
            
            options.add(autostartCheckbox, gbc);
        }

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

        JButton saveAndClose = new JButton(LangManager.getItem("config_label_saveclose"));
        saveAndClose.addActionListener(e -> {
            globalSave(config);

            for(CustomWindowListener listener : listeners)
                listener.windowClosed();
            close();
        });

        gbc.gridx = 0;
        gbc.insets.top = 20;
        options.add(saveButton, gbc);
        gbc.gridx = 1;
        options.add(saveAndClose, gbc);

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

        SnipSniper.getConfig().loadFromConfig(config);
        config.save();

        if(didThemeChange) {
            doRestartProfiles = true;
            SnipSniper.refreshTheme();
        }

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

    @Override
    public void close() {
        for(CustomWindowListener listener : listeners)
            listener.windowClosed();
        for(IClosable wnd : cWindows)
            wnd.close();
        dispose();
    }

    public void addCustomWindowListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
