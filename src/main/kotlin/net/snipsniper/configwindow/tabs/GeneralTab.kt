package net.snipsniper.configwindow.tabs

import net.snipsniper.ImageManager.Companion.getImage
import net.snipsniper.LangManager.Companion.getItem
import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.ConfigWindow.PAGE
import net.snipsniper.configwindow.HotKeyButton
import net.snipsniper.configwindow.iconwindow.IconWindow
import net.snipsniper.configwindow.textpreviewwindow.FolderPreviewRenderer
import net.snipsniper.configwindow.textpreviewwindow.SaveFormatPreviewRenderer
import net.snipsniper.configwindow.textpreviewwindow.SaveFormatPreviewRenderer.Companion.DEFAULT_FORMAT
import net.snipsniper.configwindow.textpreviewwindow.TextPreviewWindow
import net.snipsniper.utils.*
import net.snipsniper.utils.Function
import net.snipsniper.utils.ImageUtils.Companion.getDefaultIcon
import net.snipsniper.utils.ImageUtils.Companion.getIconDynamically
import net.snipsniper.utils.Utils.Companion.constructFilename
import net.snipsniper.utils.Utils.Companion.replaceVars
import net.snipsniper.utils.Utils.Companion.showPopup
import net.snipsniper.utils.WikiManager.Companion.getContent
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.ccutils.utils.CCStringUtils
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.*
import java.io.File
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeEvent

class GeneralTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    private var isDirty = false

    override fun setup(configOriginal: Config?) {
        removeAll()
        isDirty = false
        var colorChooser: CCColorChooser? = null
        var cleanDirtyFunction: Function? = null
        val config: Config
        var disablePage = false
        if (configOriginal != null) {
            config = Config(configOriginal)
            if (configOriginal.getFilename().contains("viewer") || configOriginal.getFilename()
                    .contains("editor")
            ) disablePage = true
        } else {
            config = Config("disabled_cfg.cfg", "profile_defaults.cfg")
            disablePage = true
        }
        val gbc = GridBagConstraints()
        val options = JPanel(GridBagLayout())
        val dropdown = configWindow.setupProfileDropdown(options, this, configOriginal, config, PAGE.generalPanel, "editor", "viewer")

        //profile title setting
        kotlin.run {
            gbc.gridx = 0
            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.BOTH
            gbc.insets = Insets(0, 10, 0, 10)
            options.add(configWindow.createJLabel(getItem("config_label_title"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val titleContent = JPanel(GridLayout(0, 2))
            val titleInput = JTextField(config.getString(ConfigHelper.PROFILE.title))
            titleInput.addFocusListener(object : FocusAdapter() {
                override fun focusLost(e: FocusEvent) {
                    super.focusLost(e)
                    if (CCStringUtils.removeWhitespace(titleInput.text).isEmpty()) titleInput.text = "none"
                    config.set(ConfigHelper.PROFILE.title, titleInput.text)
                }
            })
            titleContent.add(titleInput)
            val titleReset = JButton(getItem("config_label_reset"))
            titleReset.addActionListener {
                titleInput.text = "none"
                config.set(ConfigHelper.PROFILE.title, titleInput.text)
            }
            titleContent.add(titleReset)
            options.add(titleContent, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/title.json")), gbc)
        }

        //Icon setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Icon", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val iconPanel = JPanel(GridLayout(0, 2))
            val iconButton = JButton(getItem("config_label_seticon"))
            val item = dropdown.selectedItem as DropdownItem
            if (item != null) {
                val icon = (dropdown.selectedItem as DropdownItem).icon
                if (icon != null) iconButton.icon = icon
            }
            iconButton.addActionListener {
                configWindow.addCWindow(IconWindow("Custom Profile Icon", configWindow, {
                    config.set(ConfigHelper.PROFILE.icon, it[0])
                    //TODO: We have duplicated code here, uuuuugh. I miss having functions in functions :(
                    var img = getIconDynamically(config)
                    if (img == null) img = getDefaultIcon(configWindow.getIDFromFilename(config.getFilename()))
                    iconButton.icon = ImageIcon(img.getScaledInstance(16, 16, 0))
                    cleanDirtyFunction?.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                }))
            }
            iconPanel.add(iconButton)
            val iconReset = JButton(getItem("config_label_reset"))
            iconReset.addActionListener {
                //TODO: Barf, duplicated code
                config.set(ConfigHelper.PROFILE.icon, "none")
                var img = getIconDynamically(config)
                if (img == null) img = getDefaultIcon(configWindow.getIDFromFilename(config.getFilename()))
                iconButton.icon = ImageIcon(img!!.getScaledInstance(16, 16, 0))
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            iconPanel.add(iconReset)
            options.add(iconPanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/icon.json")), gbc)
        }

        //Hotkey setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_hotkey"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val hotkeyPanel = JPanel(GridLayout(0, 2))
            val hotKeyButton = HotKeyButton(config.getString(ConfigHelper.PROFILE.hotkey))
            hotKeyButton.addChangeListener {
                (if(hotKeyButton.hotkey != -1) hotKeyButton.getHotKeyString() else "NONE").also { newValue -> config.set(ConfigHelper.PROFILE.hotkey, newValue) }
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hotkeyPanel.add(hotKeyButton)
            val deleteHotKey = JButton(getItem("config_label_delete"))
            deleteHotKey.addActionListener {
                hotKeyButton.text = getItem("config_label_none")
                hotKeyButton.hotkey = -1
                config.set(ConfigHelper.PROFILE.hotkey, "NONE")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hotkeyPanel.add(deleteHotKey)
            options.add(hotkeyPanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/hotkey.json")), gbc)
        }

        //Tint color setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Tint color", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val tintColor = config.getColor(ConfigHelper.PROFILE.tintColor)
            val tintColorButton = GradientJButton("Color", tintColor)
            tintColor.addChangeListener { e: ChangeEvent ->
                config.set(ConfigHelper.PROFILE.tintColor, (e.source as CCColor).toSaveString())
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            tintColorButton.addActionListener {
                val x = configWindow.location.x + width / 2
                val y = configWindow.location.y + height / 2
                var image = getImage("preview/code_light.png")
                if (SnipSniper.config.getString(ConfigHelper.MAIN.theme) == "dark") image =
                    getImage("preview/code_dark.png")
                val chooser = CCColorChooser(tintColor, "Tint Color", x, y, false, image, null)
                configWindow.addCWindow(chooser)
            }
            options.add(tintColorButton, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //save-images toggle setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_saveimages"), JLabel.RIGHT, JLabel.CENTER), gbc)
            val saveToDisk = JCheckBox()
            saveToDisk.isSelected = config.getBool(ConfigHelper.PROFILE.saveToDisk)
            saveToDisk.addActionListener {
                config.set(ConfigHelper.PROFILE.saveToDisk, saveToDisk.isSelected.toString() + "")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            gbc.gridx = 1
            options.add(saveToDisk, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/saveimage.json")), gbc)
        }

        //Copy to clipboard setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_copyclipboard"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val copyToClipboard = JCheckBox()
            copyToClipboard.isSelected = config.getBool(ConfigHelper.PROFILE.copyToClipboard)
            copyToClipboard.addActionListener {
                config.set(ConfigHelper.PROFILE.copyToClipboard, copyToClipboard.isSelected.toString() + "")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(copyToClipboard, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/copyimage.json")), gbc)
        }

        //border-size setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_bordersize"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val borderSizePanel = JPanel(GridLayout(0, 2))
            val borderSize = JSpinner(SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.borderSize).toDouble(), 0.0, 999.0, 1.0)) //TODO: Extend JSpinner class to notify user of too large number
            borderSize.addChangeListener {
                config.set(ConfigHelper.PROFILE.borderSize, (borderSize.value as Double).toInt().toString() + "")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            borderSizePanel.add(borderSize)
            val borderColor = CCColor.fromSaveString(config.getString(ConfigHelper.PROFILE.borderColor))
            val colorBtn = GradientJButton("Color", borderColor)
            borderColor.addChangeListener {
                config.set(ConfigHelper.PROFILE.borderColor, (it.source as CCColor).toSaveString())
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            colorBtn.addActionListener {
                if (colorChooser == null || !colorChooser!!.isDisplayable) {
                    val x = configWindow.location.x + width / 2
                    val y = configWindow.location.y + height / 2
                    colorChooser = CCColorChooser(borderColor, getItem("config_label_bordercolor"), x, y, true, null, null)
                    colorChooser!!.addWindowListener(object : WindowAdapter() {
                        override fun windowClosing(e: WindowEvent) = kotlin.run { colorChooser = null }
                    })
                    configWindow.addCWindow(colorChooser)
                }
            }
            borderSizePanel.add(colorBtn, gbc)
            options.add(borderSizePanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/bordersize.json")), gbc)
        }

        //Saveformat setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Save format", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val currentSaveFormat = config.getString(ConfigHelper.PROFILE.saveFormat)
            val saveFormatButton = JButton(constructFilename(currentSaveFormat, ""))
            saveFormatButton.addActionListener {
                val saveFormatRenderer = SaveFormatPreviewRenderer(512, 256)
                val saveFormatPreview = TextPreviewWindow(
                    "Save format",
                    config.getString(ConfigHelper.PROFILE.saveFormat),
                    saveFormatRenderer,
                    getImage("icons/folder.png"),
                    configWindow,
                    "%hour%, %minute%, %second%, %day%, %month%, %year%, %random%"
                )
                saveFormatRenderer.textPreviewWindow = saveFormatPreview
                saveFormatPreview.onSave = IFunction {
                    var text = saveFormatPreview.text
                    if (text.isEmpty()) {
                        text = DEFAULT_FORMAT
                    }
                    config.set(ConfigHelper.PROFILE.saveFormat, text)
                    saveFormatButton.text = constructFilename(text, "")
                }
                configWindow.addCWindow(saveFormatPreview)
            }
            options.add(saveFormatButton, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //Location setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_picturelocation"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val pictureLocation = JTextField(CCStringUtils.correctSlashes(config.getRawString(ConfigHelper.PROFILE.pictureFolder)))
            pictureLocation.preferredSize = Dimension(200, pictureLocation.height)
            pictureLocation.maximumSize = Dimension(200, pictureLocation.height)
            pictureLocation.addFocusListener(object : FocusAdapter() {
                override fun focusLost(focusEvent: FocusEvent) {
                    val saveLocationRaw = pictureLocation.text
                    CCStringUtils.correctSlashes(saveLocationRaw)
                    val saveLocationFinal = replaceVars(saveLocationRaw)
                    val saveLocationCheck = File(saveLocationFinal)
                    if (!saveLocationCheck.exists()) {
                        cleanDirtyFunction!!.run(ConfigSaveButtonState.NO_SAVE)
                        val dialogResult = showPopup(configWindow, getItem("config_sanitation_directory_notexist") + " Create?", getItem("config_sanitation_error"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, getImage("icons/folder.png"), true)
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            val allow = File(saveLocationFinal).mkdirs()
                            if (!allow) {
                                configWindow.msgError(getItem("config_sanitation_failed_createdirectory"))
                                cleanDirtyFunction!!.run(ConfigSaveButtonState.NO_SAVE)
                            } else {
                                config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationRaw)
                                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                                cleanDirtyFunction!!.run(ConfigSaveButtonState.YES_SAVE)
                            }
                        } else {
                            if (configOriginal != null) pictureLocation.text = configOriginal.getRawString(ConfigHelper.PROFILE.pictureFolder)
                        }
                    } else {
                        cleanDirtyFunction!!.run(ConfigSaveButtonState.YES_SAVE)
                        config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationRaw)
                    }
                }
            })
            options.add(pictureLocation, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/imagefolder.json")), gbc)
        }

        //Save folder modifier setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Save folder modifier", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val customSaveButton = JButton(CCStringUtils.formatDateTimeString(config.getString(ConfigHelper.PROFILE.saveFolderCustom)))
            customSaveButton.addActionListener {
                val renderer = FolderPreviewRenderer(512, 512)
                val preview = TextPreviewWindow("Custom save folder modifier", config.getString(ConfigHelper.PROFILE.saveFolderCustom), renderer, getImage("icons/folder.png"), configWindow, "%day% = 1, %month% = 8, %year% = 2021")
                configWindow.addCWindow(preview)
                renderer.textPreviewWindow = preview
                preview.onSave = IFunction {
                    var text = preview.text
                    if (text.isEmpty()) text = "/"
                    config.set(ConfigHelper.PROFILE.saveFolderCustom, text)
                    customSaveButton.text = CCStringUtils.formatDateTimeString(text)
                    cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                }
            }
            options.add(customSaveButton, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/savefoldermodifier.json")), gbc)
        }

        //Capture delay setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_snapdelay"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val snipeDelay =
                JSpinner(SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.snipeDelay).toDouble(), 0.0, 100.0, 1.0))
            snipeDelay.addChangeListener {
                config.set(ConfigHelper.PROFILE.snipeDelay, (snipeDelay.value as Double).toInt().toString() + "")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(snipeDelay, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/snapdelay.json")), gbc)
        }

        //Open editor after capture setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_openeditor"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val openEditor = JCheckBox()
            openEditor.isSelected = config.getBool(ConfigHelper.PROFILE.openEditor)
            openEditor.addActionListener {
                config.set(ConfigHelper.PROFILE.openEditor, openEditor.isSelected.toString() + "")
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(openEditor, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/openeditor.json")), gbc)
        }

        //Spyglass settings
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_spyglass"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val spyglassDropdownEnabled = JComboBox<Any>(arrayOf("config_label_disabled".translate(), "config_label_enabled".translate(), "config_label_hold".translate(), "config_label_toggle".translate()))
            val spyglassDropdownHotkey = JComboBox<Any>(arrayOf("config_label_control".translate(), "config_label_shift".translate()))
            val startMode = config.getString(ConfigHelper.PROFILE.spyglassMode)
            val startEnabled = config.getBool(ConfigHelper.PROFILE.enableSpyglass)
            spyglassDropdownHotkey.isVisible = false
            if (startMode != "none" && startEnabled) {
                when (startMode) {
                    "hold" -> spyglassDropdownEnabled.setSelectedIndex(2)
                    "toggle" -> spyglassDropdownEnabled.setSelectedIndex(3)
                }
                spyglassDropdownHotkey.setVisible(true)
            } else if (startMode == "none" && startEnabled) {
                spyglassDropdownEnabled.setSelectedIndex(1)
            } else {
                spyglassDropdownEnabled.setSelectedIndex(0)
            }
            when (config.getInt(ConfigHelper.PROFILE.spyglassHotkey)) {
                KeyEvent.VK_CONTROL -> spyglassDropdownHotkey.setSelectedIndex(0)
                KeyEvent.VK_SHIFT -> spyglassDropdownHotkey.setSelectedIndex(1)
            }
            spyglassDropdownEnabled.addItemListener {
                val enableSpyglass: Boolean
                val spyglassMode: String
                when (spyglassDropdownEnabled.selectedIndex) {
                    0 -> {
                        spyglassDropdownHotkey.isVisible = false
                        enableSpyglass = false
                        spyglassMode = "none"
                    }
                    1 -> {
                        spyglassDropdownHotkey.isVisible = false
                        enableSpyglass = true
                        spyglassMode = "none"
                    }
                    2 -> {
                        spyglassDropdownHotkey.isVisible = true
                        enableSpyglass = true
                        spyglassMode = "hold"
                    }
                    3 -> {
                        spyglassDropdownHotkey.isVisible = true
                        enableSpyglass = true
                        spyglassMode = "toggle"
                    }
                    else -> {
                        enableSpyglass = false
                        spyglassMode = "none"
                    }
                }
                config.set(ConfigHelper.PROFILE.enableSpyglass, enableSpyglass)
                config.set(ConfigHelper.PROFILE.spyglassMode, spyglassMode)
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            spyglassDropdownHotkey.addItemListener {
                when (spyglassDropdownHotkey.selectedIndex) {
                    0 -> config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_CONTROL)
                    1 -> config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_SHIFT)
                }
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            JPanel(configWindow.getGridLayoutWithMargin(0, 2, 0)).also { spyglassPanel ->
                spyglassPanel.add(spyglassDropdownEnabled)
                spyglassPanel.add(spyglassDropdownHotkey)
                options.add(spyglassPanel, gbc)
            }
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/usespyglass.json")), gbc)
        }

        //Spyglass zoom setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_spyglasszoom"), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val spyglassZoomDropdown = JComboBox<Any>(arrayOf("8x8", "16x16", "32x32", "64x64"))
            when (config.getInt(ConfigHelper.PROFILE.spyglassZoom)) {
                8 -> spyglassZoomDropdown.setSelectedIndex(0)
                16 -> spyglassZoomDropdown.setSelectedIndex(1)
                32 -> spyglassZoomDropdown.setSelectedIndex(2)
                64 -> spyglassZoomDropdown.setSelectedIndex(3)
            }
            spyglassZoomDropdown.addItemListener {
                var zoom = 16
                when (spyglassZoomDropdown.selectedIndex) {
                    0 -> zoom = 8
                    1 -> zoom = 16
                    2 -> zoom = 32
                    3 -> zoom = 64
                }
                config.set(ConfigHelper.PROFILE.spyglassZoom, zoom)
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(spyglassZoomDropdown, gbc)
            gbc.gridx = 2
            options.add(InfoButton(getContent("config/general/spyglasszoom.json")), gbc)
        }

        //Afterdrag setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("After Drag", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val afterDragDropdownMode = JComboBox<Any>(arrayOf("config_label_disabled".translate(), "config_label_enabled".translate(), "config_label_hold".translate()))
            val afterDragDropdownHotkey = JComboBox<Any>(arrayOf(getItem("config_label_control"), getItem("config_label_shift")))
            when (config.getString(ConfigHelper.PROFILE.afterDragMode).lowercase(Locale.getDefault())) {
                "none" -> {
                    afterDragDropdownMode.selectedIndex = 0
                    afterDragDropdownHotkey.setVisible(false)
                }
                "enabled" -> {
                    afterDragDropdownMode.selectedIndex = 1
                    afterDragDropdownHotkey.setVisible(false)
                }
                "hold" -> afterDragDropdownMode.setSelectedIndex(2)
            }
            when (config.getInt(ConfigHelper.PROFILE.afterDragHotkey)) {
                KeyEvent.VK_CONTROL -> afterDragDropdownHotkey.setSelectedIndex(0)
                KeyEvent.VK_SHIFT -> afterDragDropdownHotkey.setSelectedIndex(1)
            }
            afterDragDropdownMode.addItemListener {
                when (afterDragDropdownMode.selectedIndex) {
                    0 -> {
                        config.set(ConfigHelper.PROFILE.afterDragMode, "none")
                        afterDragDropdownHotkey.setVisible(false)
                    }
                    1 -> {
                        config.set(ConfigHelper.PROFILE.afterDragMode, "enabled")
                        afterDragDropdownHotkey.setVisible(false)
                    }
                    2 -> {
                        config.set(ConfigHelper.PROFILE.afterDragMode, "hold")
                        afterDragDropdownHotkey.setVisible(true)
                    }
                }
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            afterDragDropdownHotkey.addItemListener {
                when (afterDragDropdownHotkey.selectedIndex) {
                    0 -> {
                        config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_CONTROL)
                        config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_SHIFT)
                    }
                    1 -> config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_SHIFT)
                }
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            val afterDragPanel = JPanel(GridLayout(0, 2))
            afterDragPanel.add(afterDragDropdownMode)
            afterDragPanel.add(afterDragDropdownHotkey)
            options.add(afterDragPanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //Afterdrag deadzon setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("AfterDrag deadzone", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val afterDragDeadzoneSpinner = JSpinner(SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.afterDragDeadzone), 1, 50, 1))
            afterDragDeadzoneSpinner.addChangeListener {
                config.set(ConfigHelper.PROFILE.afterDragDeadzone, afterDragDeadzoneSpinner.value.toString().toInt())
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(afterDragDeadzoneSpinner, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //Afterdrag dotted line setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Enable dotted outline", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val enableOutline = JCheckBox()
            enableOutline.isSelected = config.getBool(ConfigHelper.PROFILE.dottedOutline)
            enableOutline.addActionListener {
                config.set(ConfigHelper.PROFILE.dottedOutline, enableOutline.isSelected)
                cleanDirtyFunction!!.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(enableOutline, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //BEGIN SAVE
        cleanDirtyFunction = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true)
        //END SAVE
        add(options)
        if (disablePage) configWindow.setEnabledAll(options, false, dropdown)
    }

    override fun getPage(): PAGE {
        return PAGE.generalPanel
    }

    override fun setDirty(isDirty: Boolean) {
        this.isDirty = isDirty
    }

    override fun isDirty(): Boolean {
        return isDirty
    }
}