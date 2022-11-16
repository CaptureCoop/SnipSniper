package net.snipsniper.configwindow.tabs

import net.snipsniper.ImageManager
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.StampJPanel
import net.snipsniper.sceditor.stamps.*
import net.snipsniper.sceditor.stamps.IStamp
import net.snipsniper.sceditor.stamps.StampType
import net.snipsniper.utils.ConfigSaveButtonState
import net.snipsniper.utils.Function
import net.snipsniper.utils.InfoButton
import net.snipsniper.utils.translate
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.GridLayout
import java.awt.Insets
import java.awt.event.ItemEvent
import javax.swing.*

class EditorTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    override var isDirty = false
    override val page = ConfigWindow.PAGE.EditorPanel

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
        val dropdown: JComponent = configWindow.setupProfileDropdown(options, this, configOriginal, config, ConfigWindow.PAGE.EditorPanel, "viewer")
        //BEGIN ELEMENTS
        JCheckBox().also { ezModeCheckBox ->
            gbc.gridx = 0
            options.add(configWindow.createJLabel("EzMode", JLabel.RIGHT, JLabel.CENTER), gbc)
            gbc.gridx = 1
            ezModeCheckBox.isSelected = config.getBool(ConfigHelper.PROFILE.ezMode)
            ezModeCheckBox.addChangeListener { config.set(ConfigHelper.PROFILE.ezMode, ezModeCheckBox.isSelected) }
            options.add(ezModeCheckBox, gbc)
            gbc.gridx = 2
            options.add(InfoButton(null), gbc)
        }

        JSlider(JSlider.HORIZONTAL).also { hsvSlider ->
            gbc.gridx = 0
            options.add(configWindow.createJLabel("config_label_hsvspeed".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
            val hsvPercentage = JLabel(config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed).toString() + "%")
            hsvPercentage.horizontalAlignment = JLabel.CENTER

            hsvSlider.minimum = -100
            hsvSlider.maximum = 100
            hsvSlider.snapToTicks = true
            hsvSlider.addChangeListener {
                hsvPercentage.text = hsvSlider.value.toString() + "%"
                config.set(ConfigHelper.PROFILE.hsvColorSwitchSpeed, hsvSlider.value.toString() + "")
                saveButtonUpdate?.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            hsvSlider.value = config.getInt(ConfigHelper.PROFILE.hsvColorSwitchSpeed)
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
        val stamp = StampType.getByIndex(0).getIStamp(config, null)
        val row3_stampConfig = JPanel(GridBagLayout())
        val row3_stampPreview = StampJPanel(stamp, ImageManager.getCodePreview(), 10)
        val onUpdate = arrayOf<Function?>(null)
        val stampTitles = arrayOfNulls<String>(StampType.values().size)
        for (i in stampTitles.indices) {
            stampTitles[i] = StampType.values()[i].title
        }
        val stampDropdown = JComboBox<Any?>(stampTitles)
        stampDropdown.addItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val newStamp = StampType.getByIndex(stampDropdown.selectedIndex).getIStamp(config, null)
                row3_stampPreview.stamp = newStamp
                setupStampConfigPanel(row3_stampConfig, newStamp, row3_stampPreview, config, onUpdate[0])
                saveButtonUpdate?.run()
            }
        }
        options.add(stampDropdown, gbc)
        gbc.gridx = 1
        val previewToggleAndLabel = JPanel(GridLayout(0, 2))
        previewToggleAndLabel.add(configWindow.createJLabel("config_label_preview".translate(), JLabel.RIGHT, JLabel.CENTER))
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

    //This saves us alot of duplicate code :^)
    private class Creator(
        private val configWindow: ConfigWindow,
        private val panel: JPanel,
        private var minOverride: Double = 1.0,
        private var maxOverride: Double = 999.0,
        private var stepSizeOverride: Double = 1.0,
        private val previewPanel: StampJPanel,
        private val config: Config,
        private val constraints: GridBagConstraints,
        private val stampIndex: Int,
        private val onUpdate: Function?
    ) {
        private fun setupStampConfigPanelSpinner(configKey: ConfigHelper.PROFILE, min: Double, max: Double, stepSize: Double, previewPanel: StampJPanel, config: Config, stampIndex: Int, onUpdate: Function?): JSpinner {
            return JSpinner(SpinnerNumberModel((config.getFloat(configKey).toString() + "").toDouble(), min, max, stepSize)).also { spinner ->
                spinner.addChangeListener {
                    config.set(configKey, spinner.value.toString())
                    previewPanel.stamp = StampType.getByIndex(stampIndex).getIStamp(config, null)
                    onUpdate!!.run()
                }
            }
        }

        //setupStampConfigPanelSpinnerWithLabel
        fun setup(title: String, configKey: ConfigHelper.PROFILE, infoText: String? = null, min: Double = minOverride, max: Double = maxOverride, stepSize: Double = stepSizeOverride) {
            constraints.gridx = 0
            panel.add(configWindow.createJLabel(title, JLabel.RIGHT, JLabel.CENTER), constraints)
            constraints.gridx = 1
            panel.add(setupStampConfigPanelSpinner(configKey, min, max, stepSize, previewPanel, config, stampIndex, onUpdate), constraints)
            constraints.gridx = 2
            panel.add(InfoButton(infoText), constraints)
            constraints.gridx = 0
        }
    }

    private fun setupStampConfigPanel(panel: JPanel, stamp: IStamp?, previewPanel: StampJPanel, config: Config, onUpdate: Function?) {
        panel.removeAll()
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.insets = Insets(0, 4, 0, 4)
        when(stamp) {
            is CubeStamp -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCubeDefaultColor) {
                    previewPanel.stamp = CubeStamp(config, null)
                    onUpdate!!.run() }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                panel.add(configWindow.createJLabel("Smart Pixel", JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                val smartPixelCheckBox = JCheckBox()
                smartPixelCheckBox.isSelected = config.getBool(ConfigHelper.PROFILE.editorStampCubeSmartPixel)
                smartPixelCheckBox.addActionListener {
                    config.set(ConfigHelper.PROFILE.editorStampCubeSmartPixel, smartPixelCheckBox.isSelected.toString() + "")
                    onUpdate!!.run()
                }
                panel.add(smartPixelCheckBox, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.CUBE.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_startwidth".translate(), ConfigHelper.PROFILE.editorStampCubeWidth)
                    it.setup("config_label_startheight".translate(), ConfigHelper.PROFILE.editorStampCubeHeight)
                    it.setup("config_label_widthspeed".translate(), ConfigHelper.PROFILE.editorStampCubeWidthSpeed)
                    it.setup("config_label_heightspeed".translate(), ConfigHelper.PROFILE.editorStampCubeHeightSpeed)
                    it.setup("config_label_widthminimum".translate(), ConfigHelper.PROFILE.editorStampCubeWidthMinimum)
                    it.setup("config_label_heightminimum".translate(), ConfigHelper.PROFILE.editorStampCubeHeightMinimum)
                }
            }
            is CounterStamp -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCounterDefaultColor) { previewPanel.stamp = CounterStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                val c = Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.COUNTER.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_startwidth".translate(), ConfigHelper.PROFILE.editorStampCounterWidth)
                    it.setup("config_label_startheight".translate(), ConfigHelper.PROFILE.editorStampCounterHeight)
                    it.setup("config_label_generalspeed".translate(), ConfigHelper.PROFILE.editorStampCounterSpeed)
                    it.setup("config_label_widthspeed".translate(), ConfigHelper.PROFILE.editorStampCounterWidthSpeed)
                    it.setup("config_label_heightspeed".translate(), ConfigHelper.PROFILE.editorStampCounterHeightSpeed)
                    it.setup("config_label_widthminimum".translate(), ConfigHelper.PROFILE.editorStampCounterWidthMinimum)
                    it.setup("config_label_heightminimum".translate(), ConfigHelper.PROFILE.editorStampCounterHeightMinimum)
                }
                panel.add(configWindow.createJLabel("config_label_solidcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                val cbSolidColor = JCheckBox()
                cbSolidColor.isSelected = config.getBool(ConfigHelper.PROFILE.editorStampCounterSolidColor)
                cbSolidColor.addChangeListener {
                    config.set(ConfigHelper.PROFILE.editorStampCounterSolidColor, cbSolidColor.isSelected.toString() + "")
                    previewPanel.stamp = CounterStamp(config)
                    onUpdate!!.run()
                }
                gbc.gridx = 1
                panel.add(cbSolidColor, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                gbc.gridx = 0
                panel.add(configWindow.createJLabel("config_label_stampborder".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                val cbBorder = JCheckBox()
                cbBorder.isSelected = config.getBool(ConfigHelper.PROFILE.editorStampCounterBorderEnabled)
                cbBorder.addChangeListener {
                    config.set(ConfigHelper.PROFILE.editorStampCounterBorderEnabled, cbBorder.isSelected.toString() + "")
                    previewPanel.stamp = CounterStamp(config)
                    onUpdate!!.run()
                }
                panel.add(cbBorder, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                c.setup("config_label_fontsizemodifier".translate(), ConfigHelper.PROFILE.editorStampCounterFontSizeModifier, min = 0.1, max = 10.0, stepSize = 0.01)
                c.setup("config_label_bordermofizier".translate(), ConfigHelper.PROFILE.editorStampCounterBorderModifier)
            }
            is CircleStamp -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampCircleDefaultColor) { previewPanel.stamp = CircleStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.CIRCLE.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_startwidth".translate(), ConfigHelper.PROFILE.editorStampCircleWidth)
                    it.setup("config_label_startheight".translate(), ConfigHelper.PROFILE.editorStampCircleHeight)
                    it.setup("config_label_generalspeed".translate(), ConfigHelper.PROFILE.editorStampCircleSpeed)
                    it.setup("config_label_widthspeed".translate(), ConfigHelper.PROFILE.editorStampCircleWidthSpeed)
                    it.setup("config_label_heightspeed".translate(), ConfigHelper.PROFILE.editorStampCircleHeightSpeed)
                    it.setup("config_label_widthminimum".translate(), ConfigHelper.PROFILE.editorStampCircleWidthMinimum)
                    it.setup("config_label_heightminimum".translate(), ConfigHelper.PROFILE.editorStampCircleHeightMinimum)
                    it.setup("config_label_thickness".translate(), ConfigHelper.PROFILE.editorStampCircleThickness)
                }
            }
            is SimpleBrush -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampSimpleBrushDefaultColor) { previewPanel.stamp = SimpleBrush(config, null) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.SIMPLE_BRUSH.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_brushsize".translate(), ConfigHelper.PROFILE.editorStampSimpleBrushSize)
                    it.setup("config_label_brushsizespeed".translate(), ConfigHelper.PROFILE.editorStampSimpleBrushSizeSpeed)
                    it.setup("config_label_linepointdistance".translate(), ConfigHelper.PROFILE.editorStampSimpleBrushDistance)
                }
                panel.add(JPanel()) //Padding
            }
            is TextStamp -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampTextDefaultColor) { previewPanel.stamp = TextStamp(config, null) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.TEXT.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_defaultfontsize".translate(), ConfigHelper.PROFILE.editorStampTextDefaultFontSize)
                    it.setup("config_label_fontsizechangespeed".translate(), ConfigHelper.PROFILE.editorStampTextDefaultSpeed)
                }
                for (i in 0..5) panel.add(JPanel(), gbc) //Padding
                //TODO: Draw it in the middle, possibly by giving TextStamp a getTextWidth() function and adding an edgecase to the Stamp Renderer, to move it to the left
            }
            is RectangleStamp -> {
                panel.add(configWindow.createJLabel("config_label_startcolor".translate(), JLabel.RIGHT, JLabel.CENTER), gbc)
                gbc.gridx = 1
                panel.add(configWindow.setupColorButton("Color", config, ConfigHelper.PROFILE.editorStampRectangleDefaultColor) { previewPanel.stamp = RectangleStamp(config) }, gbc)
                gbc.gridx = 2
                panel.add(InfoButton(null), gbc)
                Creator(configWindow, panel, previewPanel = previewPanel, config = config, stampIndex = StampType.RECTANGLE.index, constraints = gbc, onUpdate = onUpdate).also {
                    it.setup("config_label_startwidth".translate(), ConfigHelper.PROFILE.editorStampRectangleWidth)
                    it.setup("config_label_startheight".translate(), ConfigHelper.PROFILE.editorStampRectangleHeight)
                    it.setup("config_label_widthspeed".translate(), ConfigHelper.PROFILE.editorStampRectangleWidthSpeed)
                    it.setup("config_label_heightspeed".translate(), ConfigHelper.PROFILE.editorStampRectangleHeightSpeed)
                    it.setup("config_label_widthminimum".translate(), ConfigHelper.PROFILE.editorStampRectangleWidthMinimum)
                    it.setup("config_label_heightminimum".translate(), ConfigHelper.PROFILE.editorStampRectangleHeightMinimum)
                    it.setup("config_label_thickness".translate(), ConfigHelper.PROFILE.editorStampRectangleThickness)
                }
            }
            else -> {
                panel.add(configWindow.createJLabel("Coming soon", JLabel.CENTER, JLabel.CENTER))
                for (i in 0..14) panel.add(JLabel())
            }
        }
    }
}