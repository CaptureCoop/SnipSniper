package net.snipsniper.configwindow.tabs

import net.snipsniper.ImageManager.Companion.getImage
import net.snipsniper.LangManager.Companion.getItem
import net.snipsniper.SnipSniper
import net.snipsniper.SnipSniper.Companion.config
import net.snipsniper.SnipSniper.Companion.configFolder
import net.snipsniper.SnipSniper.Companion.getVersionString
import net.snipsniper.SnipSniper.Companion.imgFolder
import net.snipsniper.SnipSniper.Companion.jarFolder
import net.snipsniper.SnipSniper.Companion.mainFolder
import net.snipsniper.SnipSniper.Companion.openConfigWindow
import net.snipsniper.SnipSniper.Companion.platformType
import net.snipsniper.SnipSniper.Companion.refreshGlobalConfigFromDisk
import net.snipsniper.SnipSniper.Companion.refreshTheme
import net.snipsniper.SnipSniper.Companion.resetProfiles
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.ConfigWindow.PAGE
import net.snipsniper.configwindow.UpdateButton
import net.snipsniper.utils.ConfigSaveButtonState
import net.snipsniper.utils.FileUtils.Companion.copyFromJar
import net.snipsniper.utils.FileUtils.Companion.delete
import net.snipsniper.utils.FileUtils.Companion.deleteRecursively
import net.snipsniper.utils.FileUtils.Companion.exists
import net.snipsniper.utils.FileUtils.Companion.getFilesInFolders
import net.snipsniper.utils.FileUtils.Companion.mkdirs
import net.snipsniper.utils.Function
import net.snipsniper.utils.IFunction
import net.snipsniper.utils.PlatformType
import net.snipsniper.utils.Utils.Companion.createShellLink
import net.snipsniper.utils.Utils.Companion.getLanguageDropdown
import net.snipsniper.utils.Utils.Companion.getReleaseType
import net.snipsniper.utils.Utils.Companion.showPopup
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.awt.event.ActionEvent
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
    override val page = PAGE.globalPanel

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
        val config = Config(config)
        val importConfigs = JButton("Import Configs")
        importConfigs.addActionListener {
            val dialogResult = showPopup(configWindow, "This will overwrite all current configs. Do you want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, getImage("icons/questionmark.png"), true)
            if (dialogResult == JOptionPane.NO_OPTION) return@addActionListener
            val imgFolder = File(imgFolder)
            val cfgFolder = File(configFolder)
            delete(imgFolder)
            mkdirs(imgFolder)
            delete(cfgFolder)
            mkdirs(cfgFolder)
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
                        val filePath = Paths.get(mainFolder).resolve(ze.name)
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
            refreshGlobalConfigFromDisk()
            refreshTheme()
            resetProfiles()
            configWindow.close()
            openConfigWindow(null, PAGE.globalPanel)
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
                val mainFolder = mainFolder
                val files = getFilesInFolders(
                    mainFolder!!
                )
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
        val releaseType = getReleaseType(SnipSniper.config.getString(ConfigHelper.MAIN.updateChannel))
        val channel = releaseType.toString()
        options.add(configWindow.createJLabel(CCStringUtils.format("<html><p>Current Version: %c</p><p>Update Channel: %c</p></html>", getVersionString(), channel), JLabel.CENTER, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(UpdateButton(), gbc)
        gbc.gridx = 0
        options.add(configWindow.createJLabel(getItem("config_label_language"), JLabel.RIGHT, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(getLanguageDropdown(config.getString(ConfigHelper.MAIN.language), IFunction {
                    config.set(ConfigHelper.MAIN.language, it[0])
                    saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                }), gbc)
        val themes = arrayOf(getItem("config_label_theme_light"), getItem("config_label_theme_dark"))
        val themeDropdown = JComboBox<Any>(themes)
        var themeIndex = 0 //Light theme
        if (config.getString(ConfigHelper.MAIN.theme) == "dark") themeIndex = 1
        themeDropdown.selectedIndex = themeIndex
        themeDropdown.addItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                if (themeDropdown.selectedIndex == 0) {
                    config.set(ConfigHelper.MAIN.theme, "light")
                } else if (themeDropdown.selectedIndex == 1) {
                    config.set(ConfigHelper.MAIN.theme, "dark")
                }
                saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
        }
        gbc.gridx = 0
        options.add(configWindow.createJLabel(getItem("config_label_theme"), JLabel.RIGHT, JLabel.CENTER), gbc)
        gbc.gridx = 1
        options.add(themeDropdown, gbc)
        gbc.gridx = 0
        options.add(configWindow.createJLabel(getItem("config_label_debug"), JLabel.RIGHT, JLabel.CENTER), gbc)
        val debugCheckBox = JCheckBox()
        debugCheckBox.isSelected = config.getBool(ConfigHelper.MAIN.debug)
        debugCheckBox.addActionListener {
            config.set(ConfigHelper.MAIN.debug, debugCheckBox.isSelected.toString() + "")
            saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }
        gbc.gridx = 1
        options.add(debugCheckBox, gbc)
        var autostart: IFunction? = null
        if (SystemUtils.IS_OS_WINDOWS && platformType === PlatformType.JAR) {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Start with Windows", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val userHome = System.getProperty("user.home")
            val startup = "$userHome/AppData/Roaming/Microsoft/Windows/Start Menu/Programs/Startup/"
            val batchMain = "SnipSniper.bat"
            val linkMain = "SnipSniper.lnk"
            val icoMain = "SnipSniper.ico"
            val autostartCheckbox = JCheckBox()
            autostartCheckbox.isSelected = exists(startup + linkMain)
            autostartCheckbox.addActionListener {
                if (autostartCheckbox.isSelected) {
                    autostart = IFunction {
                        mkdirs(startup)
                        val jarFolder = jarFolder
                        copyFromJar("net/snipsniper/resources/batch/$batchMain", "$jarFolder/$batchMain")
                        copyFromJar("net/snipsniper/resources/img/icons/" + icoMain.lowercase(Locale.getDefault()), "$jarFolder/$icoMain")
                        createShellLink(startup + linkMain, jarFolder + batchMain, "$jarFolder/$icoMain")
                    }
                } else {
                    autostart = IFunction { deleteRecursively(startup + linkMain) }
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
                openConfigWindow(configWindow.lastSelectedConfig, PAGE.globalPanel)
            }
            saveButtonUpdate!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }
        saveButtonUpdate = configWindow.setupSaveButtons(options, this, gbc, config, SnipSniper.config, beforeSave, false)
        add(options)
    }

    private fun globalSave(config: Config, autostart: IFunction?) {
        val didThemeChange = config.getString(ConfigHelper.MAIN.theme) != SnipSniper.config.getString(ConfigHelper.MAIN.theme)
        val didDebugChange = config.getBool(ConfigHelper.MAIN.debug) != SnipSniper.config.getBool(ConfigHelper.MAIN.debug)
        autostart?.run()
        if (didDebugChange && !config.getBool(ConfigHelper.MAIN.debug)) {
           CCLogger.enableDebugConsole(false)
        }
        SnipSniper.config.loadFromConfig(config)
        config.save()
        if (didThemeChange) {
            refreshTheme()
        }
    }
}