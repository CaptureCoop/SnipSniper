package org.snipsniper.configwindow.tabs;

import org.apache.commons.lang3.SystemUtils;
import org.snipsniper.LangManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.config.Config;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.configwindow.UpdateButton;
import org.snipsniper.utils.*;
import org.snipsniper.utils.enums.ConfigSaveButtonState;
import org.snipsniper.utils.enums.PlatformType;
import org.snipsniper.utils.enums.ReleaseType;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class GlobalTab extends JPanel implements ITab{
    private final ConfigWindow configWindow;

    public GlobalTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {
        removeAll();

        final Function[] saveButtonUpdate = {null};

        JPanel options = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.insets.bottom = 20;

        Config config = new Config(SnipSniper.getConfig());

        JButton importConfigs = new JButton("Import Configs");
        importConfigs.addActionListener(e -> {
            int dialogResult = JOptionPane.showConfirmDialog (configWindow, "This will overwrite all current configs. Do you want to continue?","Warning", JOptionPane.YES_NO_OPTION);
            if(dialogResult == JOptionPane.NO_OPTION){
                return;
            }

            File imgFolder = new File(SnipSniper.getImageFolder());
            File cfgFolder = new File(SnipSniper.getConfigFolder());
            FileUtils.delete(imgFolder); FileUtils.mkdirs(imgFolder);
            FileUtils.delete(cfgFolder); FileUtils.mkdirs(cfgFolder);

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("ZIP File", "zip"));
            int option = fileChooser.showOpenDialog(configWindow);
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

            configWindow.refreshConfigFiles();
            SnipSniper.refreshGlobalConfigFromDisk();
            SnipSniper.refreshTheme();
            SnipSniper.resetProfiles();
            new ConfigWindow(null, ConfigWindow.PAGE.globalPanel);
            configWindow.close();
        });
        JButton exportButton = new JButton("Export Configs");
        exportButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("ZIP File","zip"));
            chooser.setSelectedFile(new File("configs.zip"));
            int option = chooser.showSaveDialog(configWindow);
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
        options.add(configWindow.createJLabel(StringUtils.format("Current Version: %c (%c)", version, channel), JLabel.CENTER, JLabel.CENTER), gbc);
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
                saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            }
        });

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_language"), JLabel.RIGHT, JLabel.CENTER), gbc);
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
                saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
            }
        });

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_theme"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(themeDropdown, gbc);

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.getItem("config_label_debug"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox debugCheckBox = new JCheckBox();
        debugCheckBox.setSelected(config.getBool(ConfigHelper.MAIN.debug));
        debugCheckBox.addActionListener(e -> {
            config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected() + "");
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        gbc.gridx = 1;
        options.add(debugCheckBox, gbc);


        if(SystemUtils.IS_OS_WINDOWS && SnipSniper.getVersion().getPlatformType() == PlatformType.JAR) {
            gbc.gridx = 0;
            options.add(configWindow.createJLabel("Start with Windows", JLabel.RIGHT, JLabel.CENTER), gbc);
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

        IFunction beforeSave = args -> {
            boolean restartConfig = !config.getString(ConfigHelper.MAIN.language).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.language));
            boolean didThemeChange = !config.getString(ConfigHelper.MAIN.theme).equals(SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme));

            globalSave(config);

            if(restartConfig || didThemeChange) {
                new ConfigWindow(configWindow.getLastSelectedConfig(), ConfigWindow.PAGE.globalPanel);
                configWindow.close();
            }
        };

        saveButtonUpdate[0] = configWindow.setupSaveButtons(options, gbc, config, SnipSniper.getConfig(), beforeSave, false);

        add(options);
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
}
