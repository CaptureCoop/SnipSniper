package net.snipsniper.configwindow.tabs

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.configwindow.ConfigWindow.PAGE
import net.snipsniper.utils.ConfigSaveButtonState
import net.snipsniper.utils.Function
import net.snipsniper.utils.InfoButton
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class ViewerTab(private val configWindow: ConfigWindow) : JPanel(), ITab {
    override var isDirty = false
    override val page = PAGE.ViewerPanel

    //TODO: Disable save button if nothing is saveable?
    //TODO: Should this be nullable? Do some testing :^)
    override fun setup(configOriginal: Config?) {
        removeAll()
        isDirty = false
        lateinit var saveButtonUpdate: Function
        val config: Config
        var disablePage = false
        if (configOriginal != null) {
            config = Config(configOriginal)
            if (configOriginal.getFilename().contains("editor")) disablePage = true
        } else {
            println("disabled")
            config = Config("disabled_cfg.cfg", "profile_defaults.cfg")
            disablePage = true
        }
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        val options = JPanel(GridBagLayout())
        val dropdown: JComponent = configWindow.setupProfileDropdown(options, this, configOriginal, config, PAGE.ViewerPanel, "editor")
        //BEGIN ELEMENTS
        gbc.gridx = 0
        gbc.gridwidth = 1
        gbc.insets = Insets(0, 10, 0, 10)
        options.add(configWindow.createJLabel("Close viewer when opening editor", JLabel.RIGHT, JLabel.CENTER), gbc)
        JCheckBox().also { closeViewerOnEditor ->
            gbc.gridx = 1
            closeViewerOnEditor.isSelected = config.getBool(ConfigHelper.PROFILE.closeViewerOnOpenEditor)
            closeViewerOnEditor.addChangeListener {
                config.set(ConfigHelper.PROFILE.closeViewerOnOpenEditor, closeViewerOnEditor.isSelected)
                saveButtonUpdate.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(closeViewerOnEditor, gbc)
        }
        gbc.gridx = 2
        options.add(InfoButton(null), gbc)
        gbc.gridx = 0
        options.add(configWindow.createJLabel("Open viewer in fullscreen", JLabel.RIGHT, JLabel.CENTER), gbc)
        JCheckBox().also { openViewerFullscreen ->
            gbc.gridx = 1
            openViewerFullscreen.isSelected = config.getBool(ConfigHelper.PROFILE.openViewerInFullscreen)
            openViewerFullscreen.addChangeListener {
                config.set(ConfigHelper.PROFILE.openViewerInFullscreen, openViewerFullscreen.isSelected)
                saveButtonUpdate.run(ConfigSaveButtonState.UPDATE_CLEAN_STATE)
            }
            options.add(openViewerFullscreen, gbc)
        }
        gbc.gridx = 2
        options.add(InfoButton(null), gbc)

        //END ELEMENTS
        saveButtonUpdate = configWindow.setupSaveButtons(options, this, gbc, config, configOriginal, null, true)
        add(options)
        if (disablePage) configWindow.setEnabledAll(options, false, dropdown)
    }
}