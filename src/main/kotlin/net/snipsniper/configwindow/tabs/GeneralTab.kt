package net.snipsniper.configwindow.tabs

import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.ConfigWindow.PAGE
import net.snipsniper.configwindow.HotKeyButton
import net.snipsniper.configwindow.iconwindow.IconWindow
import net.snipsniper.configwindow.textpreviewwindow.FolderPreviewRenderer
import net.snipsniper.configwindow.textpreviewwindow.SaveFormatPreviewRenderer
import net.snipsniper.configwindow.textpreviewwindow.TextPreviewWindow
import net.snipsniper.utils.*
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.*
import java.awt.event.*
import java.io.File
import java.lang.Exception
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeEvent

class GeneralTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    override var isDirty = false
    override val page = PAGE.GeneralPanel

    override fun setup(configOriginal: Config?) {
        removeAll()
        isDirty = false
        var colorChooser: CCColorChooser? = null
        var cleanDirtyFunction: ((ConfigSaveButtonState) -> (Boolean))? = null
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
        val dropdown = configWindow.setupProfileDropdown(options, this, configOriginal, config, PAGE.GeneralPanel, "editor", "viewer")

        //profile title setting
        kotlin.run {
            gbc.gridx = 0
            gbc.gridwidth = 1
            gbc.fill = GridBagConstraints.BOTH
            gbc.insets = Insets(0, 10, 0, 10)
            options.add(configWindow.createJLabel("config_label_title".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
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
            val titleReset = JButton("config_label_reset".translate())
            titleReset.addActionListener {
                titleInput.text = "none"
                config.set(ConfigHelper.PROFILE.title, titleInput.text)
            }
            titleContent.add(titleReset)
            options.add(titleContent, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/title.json")), gbc)
        }

        //Icon setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Icon", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1

            val iconButton = JButton("config_label_seticon".translate())
            iconButton.icon = (dropdown.selectedItem as DropdownItem?)?.icon

            fun iconChange(cfgValue: String) {
                config.set(ConfigHelper.PROFILE.icon, cfgValue)
                val img = ImageUtils.getIconDynamically(config) ?: ImageUtils.getDefaultIcon(configWindow.getIDFromFilename(config.getFilename()))
                iconButton.icon = img.scaled(16, 16).toImageIcon()
                cleanDirtyFunction?.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }

            var wnd: IconWindow? = null
            iconButton.addActionListener {
                if(wnd != null) {
                    wnd?.requestFocus()
                    return@addActionListener
                }
                IconWindow("Custom Profile Icon", configWindow).also { iWnd ->
                    iWnd.onSelect = { iconChange(it) }
                    iWnd.onClose = { wnd = null }
                    configWindow.addCWindow(iWnd)
                    wnd = iWnd
                }
            }
            JPanel(GridLayout(0, 2)).also {
                it.add(iconButton)
                JButton("config_label_reset".translate()).also { resetBtn ->
                    resetBtn.addActionListener { iconChange("none") }
                    it.add(resetBtn)
                }
                options.add(it, gbc)
            }
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/icon.json")), gbc)
        }

        //Hotkey setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_hotkey".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val hotkeyPanel = JPanel(GridLayout(0, 2))
            val hotKeyButton = HotKeyButton(config.getString(ConfigHelper.PROFILE.hotkey))
            hotKeyButton.addChangeListener {
                (if(hotKeyButton.hotkey != -1) hotKeyButton.getHotKeyString() else "NONE").also { newValue -> config.set(ConfigHelper.PROFILE.hotkey, newValue) }
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hotkeyPanel.add(hotKeyButton)
            val deleteHotKey = JButton("config_label_delete".translate())
            deleteHotKey.addActionListener {
                hotKeyButton.text = "config_label_none".translate()
                hotKeyButton.hotkey = -1
                config.set(ConfigHelper.PROFILE.hotkey, "NONE")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hotkeyPanel.add(deleteHotKey)
            options.add(hotkeyPanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/hotkey.json")), gbc)
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            tintColorButton.addActionListener {
                val image = when(SnipSniper.config.getString(ConfigHelper.MAIN.theme)) {
                    "dark" -> "preview/code_dark.png".getImage()
                    "light" ->  "preview/code_light.png".getImage()
                    else -> throw Exception("Bad theme")
                }
                configWindow.addCWindow(CCColorChooser(tintColor, "Tint Color", parent = configWindow, useGradient = false, backgroundImage = image))
            }
            options.add(tintColorButton, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        //save-images toggle setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_saveimages".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            val saveToDisk = JCheckBox()
            saveToDisk.isSelected = config.getBool(ConfigHelper.PROFILE.saveToDisk)
            saveToDisk.addActionListener {
                config.set(ConfigHelper.PROFILE.saveToDisk, saveToDisk.isSelected.toString() + "")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            gbc.gridx = 1
            options.add(saveToDisk, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/saveimage.json")), gbc)
        }

        //Copy to clipboard setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_copyclipboard".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val copyToClipboard = JCheckBox()
            copyToClipboard.isSelected = config.getBool(ConfigHelper.PROFILE.copyToClipboard)
            copyToClipboard.addActionListener {
                config.set(ConfigHelper.PROFILE.copyToClipboard, copyToClipboard.isSelected.toString() + "")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(copyToClipboard, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/copyimage.json")), gbc)
        }

        //border-size setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_bordersize".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val borderSizePanel = JPanel(GridLayout(0, 2))
            val borderSize = JSpinner(SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.borderSize).toDouble(), 0.0, 999.0, 1.0)) //TODO: Extend JSpinner class to notify user of too large number
            borderSize.addChangeListener {
                config.set(ConfigHelper.PROFILE.borderSize, (borderSize.value as Double).toInt().toString() + "")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            borderSizePanel.add(borderSize)
            val borderColor = CCColor.fromSaveString(config.getString(ConfigHelper.PROFILE.borderColor))
            val colorBtn = GradientJButton("Color", borderColor)
            borderColor.addChangeListener {
                config.set(ConfigHelper.PROFILE.borderColor, (it.source as CCColor).toSaveString())
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            colorBtn.addActionListener {
                if (colorChooser == null || !colorChooser!!.isDisplayable) {
                    colorChooser = CCColorChooser(borderColor, "config_label_bordercolor".translate(), parent = configWindow, useGradient = true)
                    colorChooser!!.addWindowListener(object : WindowAdapter() {
                        override fun windowClosing(e: WindowEvent) = kotlin.run { colorChooser = null }
                    })
                    configWindow.addCWindow(colorChooser!!)
                }
            }
            borderSizePanel.add(colorBtn, gbc)
            options.add(borderSizePanel, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/bordersize.json")), gbc)
        }

        //Saveformat setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Save format", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val currentSaveFormat = config.getString(ConfigHelper.PROFILE.saveFormat)
            val saveFormatButton = JButton(Utils.constructFilename(currentSaveFormat, ""))
            saveFormatButton.addActionListener {
                val saveFormatRenderer = SaveFormatPreviewRenderer(512, 256)
                val saveFormatPreview = TextPreviewWindow("Save format", config.getString(ConfigHelper.PROFILE.saveFormat), saveFormatRenderer, "icons/folder.png".getImage(), configWindow, "%hour%, %minute%, %second%, %day%, %month%, %year%, %random%")
                saveFormatRenderer.textPreviewWindow = saveFormatPreview
                saveFormatPreview.onSave = {
                    var text = saveFormatPreview.text
                    if (text.isEmpty()) {
                        text = SaveFormatPreviewRenderer.DEFAULT_FORMAT
                    }
                    config.set(ConfigHelper.PROFILE.saveFormat, text)
                    saveFormatButton.text = Utils.constructFilename(text, "")
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
            options.add(configWindow.createJLabel("config_label_picturelocation".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val pictureLocation = JTextField(CCStringUtils.correctSlashes(config.getRawString(ConfigHelper.PROFILE.pictureFolder)))
            pictureLocation.preferredSize = Dimension(200, pictureLocation.height)
            pictureLocation.maximumSize = Dimension(200, pictureLocation.height)
            pictureLocation.addFocusListener(object : FocusAdapter() {
                override fun focusLost(focusEvent: FocusEvent) {
                    val saveLocationRaw = pictureLocation.text
                    CCStringUtils.correctSlashes(saveLocationRaw)
                    val saveLocationFinal = Utils.replaceVars(saveLocationRaw)
                    val saveLocationCheck = File(saveLocationFinal)
                    if (!saveLocationCheck.exists()) {
                        cleanDirtyFunction!!.invoke(ConfigSaveButtonState.NO_SAVE)
                        val dialogResult = Utils.showPopup(configWindow, "config_sanitation_directory_notexist".translate() + " Create?", "config_sanitation_error".translate(), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, "icons/folder.png".getImage(), true)
                        if (dialogResult == JOptionPane.YES_OPTION) {
                            val allow = File(saveLocationFinal).mkdirs()
                            if (!allow) {
                                configWindow.msgError("config_sanitation_failed_createdirectory".translate())
                                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.NO_SAVE)
                            } else {
                                config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationRaw)
                                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.YES_SAVE)
                            }
                        } else {
                            if (configOriginal != null) pictureLocation.text = configOriginal.getRawString(ConfigHelper.PROFILE.pictureFolder)
                        }
                    } else {
                        cleanDirtyFunction!!.invoke(ConfigSaveButtonState.YES_SAVE)
                        config.set(ConfigHelper.PROFILE.pictureFolder, saveLocationRaw)
                    }
                }
            })
            options.add(pictureLocation, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/imagefolder.json")), gbc)
        }

        //Save folder modifier setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("Save folder modifier", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val customSaveButton = JButton(CCStringUtils.formatDateTimeString(config.getString(ConfigHelper.PROFILE.saveFolderCustom)))
            customSaveButton.addActionListener {
                val renderer = FolderPreviewRenderer(512, 512)
                val preview = TextPreviewWindow("Custom save folder modifier", config.getString(ConfigHelper.PROFILE.saveFolderCustom), renderer, "icons/folder.png".getImage(), configWindow, "%day% = 1, %month% = 8, %year% = 2021")
                configWindow.addCWindow(preview)
                renderer.textPreviewWindow = preview
                preview.onSave = {
                    var text = preview.text
                    if (text.isEmpty()) text = "/"
                    config.set(ConfigHelper.PROFILE.saveFolderCustom, text)
                    customSaveButton.text = CCStringUtils.formatDateTimeString(text)
                    cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
                }
            }
            options.add(customSaveButton, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/savefoldermodifier.json")), gbc)
        }

        //Capture delay setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_snapdelay".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val snipeDelay =
                JSpinner(SpinnerNumberModel(config.getInt(ConfigHelper.PROFILE.snipeDelay).toDouble(), 0.0, 100.0, 1.0))
            snipeDelay.addChangeListener {
                config.set(ConfigHelper.PROFILE.snipeDelay, (snipeDelay.value as Double).toInt().toString() + "")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(snipeDelay, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/snapdelay.json")), gbc)
        }

        //Open editor after capture setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_openeditor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val openEditor = JCheckBox()
            openEditor.isSelected = config.getBool(ConfigHelper.PROFILE.openEditor)
            openEditor.addActionListener {
                config.set(ConfigHelper.PROFILE.openEditor, openEditor.isSelected.toString() + "")
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(openEditor, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/openeditor.json")), gbc)
        }

        //Spyglass settings
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_spyglass".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            spyglassDropdownHotkey.addItemListener {
                when (spyglassDropdownHotkey.selectedIndex) {
                    0 -> config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_CONTROL)
                    1 -> config.set(ConfigHelper.PROFILE.spyglassHotkey, KeyEvent.VK_SHIFT)
                }
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            JPanel(configWindow.getGridLayoutWithMargin(0, 2, 0)).also { spyglassPanel ->
                spyglassPanel.add(spyglassDropdownEnabled)
                spyglassPanel.add(spyglassDropdownHotkey)
                options.add(spyglassPanel, gbc)
            }
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/usespyglass.json")), gbc)
        }

        //Spyglass zoom setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_spyglasszoom".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(spyglassZoomDropdown, gbc)
            gbc.gridx = 2
            options.add(InfoButton(WikiManager.getContent("config/general/spyglasszoom.json")), gbc)
        }

        //Afterdrag setting
        kotlin.run {
            gbc.gridx = 0
            options.add(configWindow.createJLabel("After Drag", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            val afterDragDropdownMode = JComboBox<Any>(arrayOf("config_label_disabled".translate(), "config_label_enabled".translate(), "config_label_hold".translate()))
            val afterDragDropdownHotkey = JComboBox<Any>(arrayOf("config_label_control".translate(), "config_label_shift".translate()))
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            afterDragDropdownHotkey.addItemListener {
                when (afterDragDropdownHotkey.selectedIndex) {
                    0 -> {
                        config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_CONTROL)
                        config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_SHIFT)
                    }
                    1 -> config.set(ConfigHelper.PROFILE.afterDragHotkey, KeyEvent.VK_SHIFT)
                }
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
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
                cleanDirtyFunction!!.invoke(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
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
}