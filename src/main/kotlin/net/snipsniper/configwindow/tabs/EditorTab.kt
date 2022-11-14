package net.snipsniper.configwindow.tabs

import net.snipsniper.ImageManager.Companion.getCodePreview
import net.snipsniper.LangManager.Companion.getItem
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper.PROFILE
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.ConfigWindow.PAGE
import net.snipsniper.configwindow.StampJPanel
import net.snipsniper.sceditor.stamps.*
import net.snipsniper.sceditor.stamps.StampType.Companion.getByIndex
import net.snipsniper.utils.ConfigSaveButtonState
import net.snipsniper.utils.Function
import net.snipsniper.utils.InfoButton
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import javax.swing.*
import javax.swing.event.ChangeEvent

class EditorTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    override var isDirty = false
    override val page = PAGE.editorPanel

    override fun setup(configOriginal: Config?) {
        removeAll()
        isDirty = false
        var saveButtonUpdate: Function? = null
        val config: Config
        var disablePage = false
        if (configOriginal != null) {
            config = Config(configOriginal)
            if (configOriginal.getFilename().contains("viewer")) disablePage = true
        } else {
            config = Config("disabled_cfg.cfg", "profile_defaults.cfg")
            disablePage = true
        }
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.insets = Insets(0, 10, 0, 10)
        val options = JPanel(GridBagLayout())
        val dropdown: JComponent = configWindow.setupProfileDropdown(options, this, configOriginal, config, PAGE.editorPanel, "viewer")
        //BEGIN ELEMENTS
        JCheckBox().also { ezModeCheckBox ->
            gbc.gridx = 0
            options.add(configWindow.createJLabel("EzMode", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            ezModeCheckBox.isSelected = config.getBool(PROFILE.ezMode)
            ezModeCheckBox.addChangeListener { config.set(PROFILE.ezMode, ezModeCheckBox.isSelected) }
            options.add(ezModeCheckBox, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        JSlider(JSlider.HORIZONTAL).also { hsvSlider ->
            gbc.gridx = 0
            options.add(configWindow.createJLabel(getItem("config_label_hsvspeed"), JLabel.RIGHT, JLabel.CENTER), gbc)
            val hsvPercentage = JLabel(config.getInt(PROFILE.hsvColorSwitchSpeed).toString() + "%")
            hsvPercentage.horizontalAlignment = JLabel.CENTER

            hsvSlider.minimum = -100
            hsvSlider.maximum = 100
            hsvSlider.snapToTicks = true
            hsvSlider.addChangeListener {
                hsvPercentage.text = hsvSlider.value.toString() + "%"
                config.set(PROFILE.hsvColorSwitchSpeed, hsvSlider.value.toString() + "")
                saveButtonUpdate?.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hsvSlider.value = config.getInt(PROFILE.hsvColorSwitchSpeed)
            gbc.gridx = 1
            options.add(hsvSlider, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
            gbc.gridx = 0
            options.add(JPanel(), gbc) //Avoid shifting around things
            gbc.gridx = 1
            options.add(hsvPercentage, gbc)
        }

        gbc.gridx = 0
        gbc.insets.top = 20
        val stamp = getByIndex(0).getIStamp(config, null)
        val row3_stampConfig = JPanel(GridBagLayout())
        val row3_stampPreview = StampJPanel(stamp, getCodePreview(), 10)
        val onUpdate = arrayOf<Function?>(null)
        val stampTitles = arrayOfNulls<String>(StampType.values().size)
        for (i in stampTitles.indices) {
            stampTitles[i] = StampType.values()[i].title
        }
        val stampDropdown = JComboBox<Any?>(stampTitles)
        stampDropdown.addItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val newStamp = getByIndex(stampDropdown.selectedIndex).getIStamp(config, null)
                row3_stampPreview.stamp = newStamp
                setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config, onUpdate[0])
                saveButtonUpdate?.run()
            }
        }
        options.add(stampDropdown, gbc)
        gbc.gridx = 1
        val previewToggleAndLabel = JPanel(GridLayout(0, 2))
        previewToggleAndLabel.add(configWindow.createJLabel(getItem("config_label_preview"), JLabel.RIGHT, JLabel.CENTER))
        val previewBGToggle = JCheckBox()
        previewBGToggle.isSelected = true
        previewBGToggle.addChangeListener { row3_stampPreview.backgroundEnabled = previewBGToggle.isSelected }
        previewToggleAndLabel.add(previewBGToggle)
        options.add(previewToggleAndLabel, gbc)
        gbc.gridx = 2
        options.add(JPanel(), gbc)
        options.add(InfoButton(null), gbc)
        gbc.gridx = 0
        gbc.insets.top = 0
        options.add(row3_stampConfig, gbc)
        gbc.gridx = 1
        options.add(row3_stampPreview, gbc)

        //END ELEMENTS
        saveButtonUpdate = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true)
        onUpdate[0] = object : Function() {
            override fun run() = saveButtonUpdate.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
        }
        setupStampConfigPanel(row3_stampConfig, stamp, row3_stampPreview, config, onUpdate[0])
        add(options)
        if (disablePage) configWindow.setEnabledAll(options, false, dropdown)
    }

    private fun setupStampConfigPanelSpinner(configKey: PROFILE, min: Double, max: Double, stepSize: Double, previewPanel: StampJPanel, config: Config, stampIndex: Int, onUpdate: Function?): JSpinner {
        return JSpinner(SpinnerNumberModel((config.getFloat(configKey).toString() + "").toDouble(), min, max, stepSize)).also { spinner ->
            spinner.addChangeListener {
                config.set(configKey, spinner.value.toString().toDouble().toInt())
                previewPanel.stamp = getByIndex(stampIndex).getIStamp(config, null)
                onUpdate!!.run()
            }
        }
    }

    private fun setupStampConfigPanelSpinnerWithLabel(panel: JPanel, title: String, configKey: PROFILE, min: Double, max: Double, stepSize: Double, previewPanel: StampJPanel, config: Config, stampIndex: Int, constraints: GridBagConstraints, infoText: String?, onUpdate: Function?) {
        constraints.gridx = 0
        panel.add(configWindow.createJLabel(title, JLabel.RIGHT, JLabel.CENTER), constraints)
        constraints.gridx = 1
        panel.add(setupStampConfigPanelSpinner(configKey, min, max, stepSize, previewPanel, config, stampIndex, onUpdate), constraints)
        constraints.gridx = 2
        panel.add(InfoButton(infoText), constraints)
        constraints.gridx = 0
    }

    private fun setupStampConfigPanel(panel: JPanel, stamp: IStamp?, previewPanel: StampJPanel, config: Config, onUpdate: Function?) {
        panel.removeAll()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.insets = Insets(0, 4, 0, 4)
        when(stamp) {
            is CubeStamp -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampCubeDefaultColor) {
                    previewPanel.stamp = CubeStamp(config, null)
                    onUpdate!!.run() }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                panel.add(configWindow.createJLabel("Smart Pixel", JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                val smartPixelCheckBox = JCheckBox()
                smartPixelCheckBox.isSelected = config.getBool(PROFILE.editorStampCubeSmartPixel)
                smartPixelCheckBox.addActionListener {
                    config.set(PROFILE.editorStampCubeSmartPixel, smartPixelCheckBox.isSelected.toString() + "")
                    onUpdate!!.run()
                }
                panel.add(smartPixelCheckBox, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                val stampIndex = StampType.CUBE.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startwidth"), PROFILE.editorStampCubeWidth, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startheight"), PROFILE.editorStampCubeHeight, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthspeed"), PROFILE.editorStampCubeWidthSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightspeed"), PROFILE.editorStampCubeHeightSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthminimum"), PROFILE.editorStampCubeWidthMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightminimum"), PROFILE.editorStampCubeHeightMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
            }
            is CounterStamp -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampCounterDefaultColor) { previewPanel.stamp = CounterStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                val stampIndex = StampType.COUNTER.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startwidth"), PROFILE.editorStampCounterWidth, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startheight"), PROFILE.editorStampCounterHeight, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_generalspeed"), PROFILE.editorStampCounterSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthspeed"), PROFILE.editorStampCounterWidthSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightspeed"), PROFILE.editorStampCounterHeightSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthminimum"), PROFILE.editorStampCounterWidthMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightminimum"), PROFILE.editorStampCounterHeightMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                panel.add(configWindow.createJLabel(getItem("config_label_solidcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                val cbSolidColor = JCheckBox()
                cbSolidColor.isSelected = config.getBool(PROFILE.editorStampCounterSolidColor)
                cbSolidColor.addChangeListener {
                    config.set(PROFILE.editorStampCounterSolidColor, cbSolidColor.isSelected.toString() + "")
                    previewPanel.stamp = CounterStamp(config)
                    onUpdate!!.run()
                }
                gbc.gridx = 1
                panel.add(cbSolidColor, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                panel.add(configWindow.createJLabel(getItem("config_label_stampborder"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                val cbBorder = JCheckBox()
                cbBorder.isSelected = config.getBool(PROFILE.editorStampCounterBorderEnabled)
                cbBorder.addChangeListener {
                    config.set(PROFILE.editorStampCounterBorderEnabled, cbBorder.isSelected.toString() + "")
                    previewPanel.stamp = CounterStamp(config)
                    onUpdate!!.run()
                }
                panel.add(cbBorder, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_fontsizemodifier"), PROFILE.editorStampCounterFontSizeModifier, 0.1, 10.0, 0.01, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_bordermofizier"), PROFILE.editorStampCounterBorderModifier, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
            }
            is CircleStamp -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampCircleDefaultColor) { previewPanel.stamp = CircleStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                val stampIndex = StampType.CIRCLE.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startwidth"), PROFILE.editorStampCircleWidth, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startheight"), PROFILE.editorStampCircleHeight, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_generalspeed"), PROFILE.editorStampCircleSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthspeed"), PROFILE.editorStampCircleWidthSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightspeed"), PROFILE.editorStampCircleHeightSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthminimum"), PROFILE.editorStampCircleWidthMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightminimum"), PROFILE.editorStampCircleHeightMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_thickness"), PROFILE.editorStampCircleThickness, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
            }
            is SimpleBrush -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampSimpleBrushDefaultColor) { previewPanel.stamp = SimpleBrush(config, null) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                val stampIndex = StampType.SIMPLE_BRUSH.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_brushsize"), PROFILE.editorStampSimpleBrushSize, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_brushsizespeed"), PROFILE.editorStampSimpleBrushSizeSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_linepointdistance"), PROFILE.editorStampSimpleBrushDistance, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                panel.add(JPanel()) //Padding
            }
            is TextStamp -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampTextDefaultColor) { previewPanel.stamp = TextStamp(config, null) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                val stampIndex = StampType.TEXT.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_defaultfontsize"), PROFILE.editorStampTextDefaultFontSize, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_fontsizechangespeed"), PROFILE.editorStampTextDefaultSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                for (i in 0..5) panel.add(JPanel(), gbc) //Padding
                //TODO: Draw it in the middle, possibly by giving TextStamp a getTextWidth() function and adding an edgecase to the Stamp Renderer, to move it to the left
            }
            is RectangleStamp -> {
                panel.add(configWindow.createJLabel(getItem("config_label_startcolor"), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, PROFILE.editorStampRectangleDefaultColor) { previewPanel.stamp = RectangleStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                val stampIndex = StampType.RECTANGLE.index
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startwidth"), PROFILE.editorStampRectangleWidth, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_startheight"), PROFILE.editorStampRectangleHeight, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthspeed"), PROFILE.editorStampRectangleWidthSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightspeed"), PROFILE.editorStampRectangleHeightSpeed, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_widthminimum"), PROFILE.editorStampRectangleWidthMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_heightminimum"), PROFILE.editorStampRectangleHeightMinimum, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
                setupStampConfigPanelSpinnerWithLabel(panel, getItem("config_label_thickness"), PROFILE.editorStampRectangleThickness, 1.0, 999.0, 1.0, previewPanel, config, stampIndex, gbc, null, onUpdate)
            }
            else -> {
                panel.add(configWindow.createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER))
                for (i in 0..14) panel.add(JLabel())
            }
        }
    }
}