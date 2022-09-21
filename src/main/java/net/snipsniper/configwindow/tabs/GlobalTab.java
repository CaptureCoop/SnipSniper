package net.snipsniper.configwindow.tabs;

import net.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;
import net.snipsniper.ImageManager;
import net.snipsniper.LangManager;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.configwindow.ConfigWindow;
import net.snipsniper.configwindow.UpdateButton;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.ccutils.utils.CCStringUtils;

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
    private boolean isDirty;

    public GlobalTab(ConfigWindow configWindow) {
        this.configWindow = configWindow;
    }

    @Override
    public void setup(Config configOriginal) {
        removeAll();
        isDirty = false;

        final Function[] saveButtonUpdate = {null};

        JPanel options = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridwidth = 1;
        gbc.insets.bottom = 20;

        Config config = new Config(SnipSniper.Companion.getConfig());

        JButton importConfigs = new JButton("Import Configs");
        importConfigs.addActionListener(e -> {
            int dialogResult = Utils.Companion.showPopup(configWindow, "This will overwrite all current configs. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ImageManager.Companion.getImage("icons/questionmark.png"), true);
            if(dialogResult == JOptionPane.NO_OPTION){
                return;
            }

            File imgFolder = new File(SnipSniper.Companion.getImgFolder());
            File cfgFolder = new File(SnipSniper.Companion.getConfigFolder());
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
                        Path filePath = Paths.get(SnipSniper.Companion.getMainFolder()).resolve(ze.getName());
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
                    CCLogger.log("Could not import zip file!", CCLogLevel.ERROR);
                    CCLogger.logStacktrace(ex, CCLogLevel.ERROR);
                }
            }

            configWindow.refreshConfigFiles();
            SnipSniper.Companion.refreshGlobalConfigFromDisk();
            SnipSniper.Companion.refreshTheme();
            SnipSniper.Companion.resetProfiles();
            configWindow.close();
            SnipSniper.Companion.openConfigWindow(null, ConfigWindow.PAGE.globalPanel);
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
                String mainFolder = SnipSniper.Companion.getMainFolder();
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
                    CCLogger.log("Could not export zip file!", CCLogLevel.ERROR);
                    CCLogger.logStacktrace(ex, CCLogLevel.ERROR);
                }
            }
        });
        options.add(importConfigs, gbc);
        gbc.gridx = 1;
        options.add(exportButton, gbc);

        gbc.gridx = 0;
        gbc.insets = new Insets(0, 10, 0, 10);
        String version = SnipSniper.Companion.getBuildInfo().getVersion().toString();
        ReleaseType releaseType = Utils.Companion.getReleaseType(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.updateChannel));
        String channel = releaseType.toString();
        options.add(configWindow.createJLabel(CCStringUtils.format("<html><p>Current Version: %c</p><p>Update Channel: %c</p></html>", version, channel), JLabel.CENTER, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(new UpdateButton(), gbc);

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.Companion.getItem("config_label_language"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(Utils.Companion.getLanguageDropdown(config.getString(ConfigHelper.MAIN.language), args -> {
            config.set(ConfigHelper.MAIN.language, args[0]);
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        }), gbc);

        String[] themes = {LangManager.Companion.getItem("config_label_theme_light"), LangManager.Companion.getItem("config_label_theme_dark")};
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
        options.add(configWindow.createJLabel(LangManager.Companion.getItem("config_label_theme"), JLabel.RIGHT, JLabel.CENTER), gbc);
        gbc.gridx = 1;
        options.add(themeDropdown, gbc);

        gbc.gridx = 0;
        options.add(configWindow.createJLabel(LangManager.Companion.getItem("config_label_debug"), JLabel.RIGHT, JLabel.CENTER), gbc);
        JCheckBox debugCheckBox = new JCheckBox();
        debugCheckBox.setSelected(config.getBool(ConfigHelper.MAIN.debug));
        debugCheckBox.addActionListener(e -> {
            config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected() + "");
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        });
        gbc.gridx = 1;
        options.add(debugCheckBox, gbc);

        IFunction[] autostart = {null};
        if(SystemUtils.IS_OS_WINDOWS && SnipSniper.Companion.getPlatformType() == PlatformType.JAR) {
            gbc.gridx = 0;
            options.add(configWindow.createJLabel("Start with Windows", JLabel.RIGHT, JLabel.CENTER), gbc);
            gbc.gridx = 1;

            final String userHome = System.getProperty("user.home");
            final String startup = userHome + "/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/";
            final String batchMain = "SnipSniper.bat";
            final String linkMain = "SnipSniper.lnk";
            final String icoMain = "SnipSniper.ico";

            JCheckBox autostartCheckbox = new JCheckBox();
            autostartCheckbox.setSelected(FileUtils.exists(startup + linkMain));

            autostartCheckbox.addActionListener(e -> {
                if(autostartCheckbox.isSelected()) {
                    autostart[0] = args -> {
                        FileUtils.mkdirs(startup);
                        String jarFolder = SnipSniper.Companion.getJarFolder();
                        FileUtils.copyFromJar("net/snipsniper/resources/batch/" + batchMain, jarFolder + "/" + batchMain);
                        FileUtils.copyFromJar("net/snipsniper/resources/img/icons/" + icoMain.toLowerCase(), jarFolder + "/" + icoMain);
                        Utils.Companion.createShellLink(startup + linkMain, jarFolder + batchMain, jarFolder + "/" + icoMain);
                    };
                } else {
                    autostart[0] = args -> FileUtils.deleteRecursively(startup + linkMain);
                }
            });

            options.add(autostartCheckbox, gbc);
        }

        IFunction beforeSave = args -> {
            boolean restartConfig = !config.getString(ConfigHelper.MAIN.language).equals(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.language));
            boolean didThemeChange = !config.getString(ConfigHelper.MAIN.theme).equals(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.theme));

            globalSave(config, autostart[0]);

            if(restartConfig || didThemeChange) {
                configWindow.close();
                SnipSniper.Companion.openConfigWindow(configWindow.getLastSelectedConfig(), ConfigWindow.PAGE.globalPanel);
            }
            saveButtonUpdate[0].run(ConfigSaveButtonState.UPDATE_CLEAN_STATE);
        };

        saveButtonUpdate[0] = configWindow.setupSaveButtons(options, this, gbc, config, SnipSniper.Companion.getConfig(), beforeSave, false);

        add(options);
    }

    @Override
    public ConfigWindow.PAGE getPage() {
        return ConfigWindow.PAGE.globalPanel;
    }

    @Override
    public void setDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    @Override
    public boolean isDirty() {
        return isDirty;
    }

    private void globalSave(Config config, IFunction autostart) {
        boolean didThemeChange = !config.getString(ConfigHelper.MAIN.theme).equals(SnipSniper.Companion.getConfig().getString(ConfigHelper.MAIN.theme));
        boolean didDebugChange = config.getBool(ConfigHelper.MAIN.debug) != SnipSniper.Companion.getConfig().getBool(ConfigHelper.MAIN.debug);

        if(autostart != null)
            autostart.run();

        if(didDebugChange && !config.getBool(ConfigHelper.MAIN.debug)){
            CCLogger.enableDebugConsole(false);
        }

        SnipSniper.Companion.getConfig().loadFromConfig(config);
        config.save();

        if(didThemeChange) {
            SnipSniper.Companion.refreshTheme();
        }
    }
}
