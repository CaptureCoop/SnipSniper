package net.snipsniper.configwindow.tabs

import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.UpdateButton
import net.snipsniper.utils.*
import net.snipsniper.utils.Function
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import java.io.*
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.swing.*
import javax.swing.filechooser.FileNameExtensionFilter

class GlobalTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    override var isDirty = false
    override val page = ConfigWindow.PAGE.GlobalPanel

    override fun setup(configOriginal: Config?) {
        removeAll()
        isDirty = false
        var saveButtonUpdate: Function? = null
        val options = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()
        gbc.gridx = 0
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridwidth = 1
        gbc.insets.bottom = 20
        val config = Config(SnipSniper.config)
        val importConfigs = JButton("Import Configs")
        importConfigs.addActionListener {
            val dialogResult = Utils.showPopup(configWindow, "This will overwrite all current configs. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, "icons/questionmark.png".getImage(), true)
            if (dialogResult == JOptionPane.NO_OPTION) return@addActionListener
            File(SnipSniper.imgFolder).also {
                it.deleteRecursively()
                it.mkdirs()
            }
            File(SnipSniper.configFolder).also {
                it.deleteRecursively()
                it.mkdirs()
            }
            val fileChooser = JFileChooser()
            fileChooser.fileFilter = FileNameExtensionFilter("ZIP File", "zip")
            val option = fileChooser.showOpenDialog(configWindow)
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    val buffer = ByteArray(4096)
                    val fis = FileInputStream(fileChooser.selectedFile)
                    val bis = BufferedInputStream(fis)
                    val zis = ZipInputStream(bis)
                    var ze: ZipEntry
                    while (zis.nextEntry.also { ze = it } != null) {
                        val filePath = Paths.get(SnipSniper.mainFolder).resolve(ze.name)
                        FileOutputStream(filePath.toFile()).use { fos ->
                            BufferedOutputStream(fos, buffer.size).use { bos ->
                                var len: Int
                                while (zis.read(buffer).also { len = it } > 0) {
                                    bos.write(buffer, 0, len)
                                }
                            }
                        }
                    }
                    fis.close()
                    bis.close()
                    zis.close()
                } catch (ex: IOException) {
                    CCLogger.error("Could not import zip file!")
                    CCLogger.logStacktrace(ex, CCLogLevel.ERROR)
                }
            }
            configWindow.refreshConfigFiles()
            SnipSniper.refreshGlobalConfigFromDisk()
            SnipSniper.refreshTheme()
            SnipSniper.resetProfiles()
            configWindow.close()
            SnipSniper.openConfigWindow(null, ConfigWindow.PAGE.GlobalPanel)
        }
        val exportButton = JButton("Export Configs")
        exportButton.addActionListener {
            val chooser = JFileChooser()
            chooser.fileFilter = FileNameExtensionFilter("ZIP File", "zip")
            chooser.selectedFile = File("configs.zip")
            val option = chooser.showSaveDialog(configWindow)
            if (option == JFileChooser.APPROVE_OPTION) {
                var path = chooser.selectedFile.absolutePath
                if (!path.endsWith(".zip")) path += ".zip"
                val zip = File(path)
                val mainFolder = SnipSniper.mainFolder
                val files = FileUtils.getFilesInFolders(mainFolder)
                try {
                    val out = ZipOutputStream(FileOutputStream(zip))
                    for (file in files) {
                        if (!file.contains("logs")) {
                            var filename = file.replace(mainFolder, "")
                            if (filename.startsWith("/")) filename = filename.substring(1)
                            val zipEntry = ZipEntry(filename)
                            out.putNextEntry(zipEntry)
                            Files.copy(File(file).toPath(), out)
                            out.closeEntry()
                        }
                    }
                    out.close()
                } catch (ex: IOException) {
                    CCLogger.error("Could not export zip file!")
                    CCLogger.logStacktrace(ex, CCLogLevel.ERROR)
                }
            }
        }
        options.add(importConfigs, gbc)
        gbc.gridx = 1
        options.add(exportButton, gbc)
        gbc.gridx = 0
        gbc.insets = Insets(0, 10, 0, 10)
        val releaseType = Utils.getReleaseType(SnipSniper.config.getString(ConfigHelper.MAIN.updateChannel))
        val channel = releaseType.toString()
        options.add(configWindow.createJLabel("<html><p>Current Version: ${SnipSniper.getVersionString()}</p><p>Update Channel: $channel</p></html>", JLabel.CENTER, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(UpdateButton(), gbc)
        gbc.gridx = 0
        options.add(configWindow.createJLabel("config_label_language".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(Utils.getLanguageDropdown(config.getString(ConfigHelper.MAIN.language)) {
            config.set(ConfigHelper.MAIN.language, it)
            saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }, gbc)
        val themes = arrayOf("config_label_theme_light".translate(), "config_label_theme_dark".translate())
        val themeDropdown = JComboBox<Any>(themes)
        themeDropdown.selectedIndex = if (config.getString(ConfigHelper.MAIN.theme) == "dark") 1 else 0
        themeDropdown.addItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                when(themeDropdown.selectedIndex) {
                    0 -> config.set(ConfigHelper.MAIN.theme, "light")
                    1 -> config.set(ConfigHelper.MAIN.theme, "dark")
                }
                saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
        }
        gbc.gridx = 0
        options.add(configWindow.createJLabel("config_label_theme".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(themeDropdown, gbc)
        gbc.gridx = 0
        options.add(configWindow.createJLabel("config_label_debug".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
        val debugCheckBox = JCheckBox()
        debugCheckBox.isSelected = config.getBool(ConfigHelper.MAIN.debug)
        debugCheckBox.addActionListener {
            config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected.toString() + "")
            saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }
        gbc.gridx = 1
        options.add(debugCheckBox, gbc)
        var autostart: (() -> (Unit))? = null
        if (SystemUtils.IS_OS_WINDOWS && SnipSniper.platformType === PlatformType.JAR) {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Start with Windows", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val userHome = System.getProperty("user.home")
            val startup = "$userHome/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/"
            val batchMain = "SnipSniper.bat"
            val linkMain = "SnipSniper.lnk"
            val icoMain = "SnipSniper.ico"
            val autostartCheckbox = JCheckBox()
            autostartCheckbox.isSelected = File(startup, linkMain).exists()
            autostartCheckbox.addActionListener {
                autostart = if (autostartCheckbox.isSelected) {
                    {
                        File(startup).mkdirs()
                        val jarFolder = SnipSniper.jarFolder
                        FileUtils.copyFromJar("net/snipsniper/resources/batch/$batchMain", "$jarFolder/$batchMain")
                        FileUtils.copyFromJar("net/snipsniper/resources/img/icons/" + icoMain.lowercase(Locale.getDefault()), "$jarFolder/$icoMain")
                        Utils.createShellLink(startup + linkMain, jarFolder + batchMain, "$jarFolder/$icoMain")
                    }
                } else {
                    { File(startup, linkMain).deleteRecursively() }
                }
            }
            options.add(autostartCheckbox, gbc)
        }
        val beforeSave = IFunction {
            val restartConfig = config.getString(ConfigHelper.MAIN.language) != SnipSniper.config.getString(ConfigHelper.MAIN.language)
            val didThemeChange = config.getString(ConfigHelper.MAIN.theme) != SnipSniper.config.getString(ConfigHelper.MAIN.theme)
            globalSave(config, autostart)
            if (restartConfig || didThemeChange) {
                configWindow.close()
                SnipSniper.openConfigWindow(configWindow.lastSelectedConfig, ConfigWindow.PAGE.GlobalPanel)
            }
            saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }
        saveButtonUpdate = configWindow.setupSaveButtons(options, this, gbc, config, SnipSniper.config, beforeSave, false)
        add(options)
    }

    private fun globalSave(config: Config, autostart: (() -> (Unit))?) {
        val didThemeChange = config.getString(ConfigHelper.MAIN.theme) != SnipSniper.config.getString(ConfigHelper.MAIN.theme)
        val didDebugChange = config.getBool(ConfigHelper.MAIN.debug) != SnipSniper.config.getBool(ConfigHelper.MAIN.debug)
        autostart?.invoke()
        if (didDebugChange && !config.getBool(ConfigHelper.MAIN.debug)) CCLogger.enableDebugConsole(false)
        SnipSniper.config.loadFromConfig(config)
        config.save()
        if (didThemeChange) SnipSniper.refreshTheme()
    }
}