package net.snipsniper.configwindow

import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper.PROFILE
import net.snipsniper.configwindow.tabs.*
import net.snipsniper.systray.Sniper
import net.snipsniper.utils.*
import net.snipsniper.utils.Function
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.*
import java.io.File
import javax.swing.*
import javax.swing.event.ChangeListener

class ConfigWindow(config: Config?, page: PAGE) : JFrame(), CCIClosable {
    private val listeners = ArrayList<CustomWindowListener>()
    private val configFiles = ArrayList<File>()
    var lastSelectedConfig: Config? = null
        private set

    enum class PAGE { GeneralPanel, EditorPanel, ViewerPanel, GlobalPanel }

    private lateinit var generalTab: GeneralTab
    private lateinit var editorTab: EditorTab
    private lateinit var viewerTab: ViewerTab
    private lateinit var globalTab: GlobalTab
    private val tabs = arrayOfNulls<ITab>(4)
    private var activeTabIndex = 0
    private var activeDropdownIndex = 0
    private val cWindows = ArrayList<CCIClosable>()

    init {
        CCLogger.info("Creating config window")
        setSize(512, 512)
        title = "config_label_config".translate()
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        iconImage = "icons/config.png".getImage()
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                if (tabs[activeTabIndex]!!.isDirty)
                    if (showDirtyWarning() == JOptionPane.NO_OPTION) return
                close()
            }
        })
        refreshConfigFiles()
        setup(config, page)
        isVisible = true
        Toolkit.getDefaultToolkit().screenSize.let { setLocation(it.width / 2 - width / 2, it.height / 2 - height / 2) }
        setSize((generalTab.width * 1.25f).toInt(), height)
    }

    fun refreshConfigFiles() {
        configFiles.clear()
        File(SnipSniper.configFolder).listFiles()?.forEach { file ->
            if (FileUtils.getFileExtension(file) == Config.DOT_EXTENSION) configFiles.add(file)
        }
    }

    private fun setup(config: Config?, page: PAGE) {
        val tabPane = JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
        val iconSize = 16
        var index = 0
        activeTabIndex = index
        lastSelectedConfig = config
        generalTab = GeneralTab(this)
        generalTab.setup(config)
        tabPane.addTab("SnipSniper", generateScrollPane(generalTab))
        tabPane.setIconAt(index, "icons/snipsniper.png".getImage().scaled(iconSize, iconSize).toImageIcon())
        tabs[index] = generalTab
        index++
        editorTab = EditorTab(this)
        editorTab.setup(config)
        tabPane.addTab("Editor", generateScrollPane(editorTab))
        tabPane.setIconAt(index, "icons/editor.png".getImage().scaled(iconSize, iconSize).toImageIcon())
        tabs[index] = editorTab
        if (page == PAGE.EditorPanel) activeTabIndex = index
        index++
        viewerTab = ViewerTab(this)
        viewerTab.setup(config)
        tabPane.addTab("Viewer", generateScrollPane(viewerTab))
        tabPane.setIconAt(index, "icons/viewer.png".getImage().scaled(iconSize, iconSize).toImageIcon())
        tabs[index] = viewerTab
        if (page == PAGE.ViewerPanel) activeTabIndex = index
        index++
        globalTab = GlobalTab(this)
        globalTab.setup(config)
        tabPane.addTab("Global", generateScrollPane(globalTab))
        tabPane.setIconAt(index, "icons/config.png".getImage().scaled(iconSize, iconSize).toImageIcon())
        tabs[index] = globalTab
        if (page == PAGE.GlobalPanel) activeTabIndex = index
        tabPane.addChangeListener {
            if (tabs[activeTabIndex]!!.isDirty) {
                tabs[activeTabIndex]!!.isDirty = false
                val requestedIndex = tabPane.selectedIndex
                tabPane.selectedIndex = activeTabIndex
                if (showDirtyWarning() == JOptionPane.YES_OPTION) {
                    setupPaneDynamic(config, tabs[activeTabIndex]!!.page)
                    setupPaneDynamic(config, tabs[requestedIndex]!!.page)
                    tabPane.selectedIndex = requestedIndex
                    return@addChangeListener
                }
                tabs[activeTabIndex]!!.isDirty = true
            }
            activeTabIndex = tabPane.selectedIndex
        }
        tabPane.selectedIndex = activeTabIndex
        add(tabPane)
    }

    fun msgError(msg: String?) {
        Utils.showPopup(this, msg!!, "config_sanitation_error".translate(), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, "icons/redx.png".getImage(), true)
    }

    private fun setupPaneDynamic(config: Config?, page: PAGE?) {
        when (page) {
            PAGE.GeneralPanel -> {
                generalTab.setup(config)
                editorTab.setup(config)
                viewerTab.setup(config)
                globalTab.setup(config)
            }

            PAGE.EditorPanel -> {
                editorTab.setup(config)
                viewerTab.setup(config)
                globalTab.setup(config)
            }

            PAGE.ViewerPanel -> {
                viewerTab.setup(config)
                globalTab.setup(config)
            }

            PAGE.GlobalPanel -> globalTab.setup(config)
            else -> throw Exception("Bad page")
        }
    }

    fun setupProfileDropdown(panelToAdd: JPanel, parentPanel: JPanel, configOriginal: Config?, config: Config, page: PAGE?, vararg blacklist: String): JComboBox<DropdownItem?> {
        //Returns the dropdown, however dont add it manually
        //TODO: Refresh other dropdowns when creating new profile?
        val profiles = ArrayList<DropdownItem>()
        for (file in configFiles) {
            if (file.name.contains("viewer")) {
                var add = true
                for (str in blacklist) if (str.contains("viewer")) {
                    add = false
                    break
                }
                if (add) profiles.add(0, DropdownItem("Standalone Viewer", file.name, "icons/viewer.png".getImage()))
            } else if (file.name.contains("editor")) {
                var add = true
                for (str in blacklist) if (str.contains("editor")) {
                    add = false
                    break
                }
                if (add) profiles.add(0, DropdownItem("Standalone Editor", file.name, "icons/editor.png".getImage()))
            } else if (file.name.contains("profile")) {
                val nr = getIDFromFilename(file.name)
                var img = ImageUtils.getIconDynamically(Config(file.name, "profile_defaults.cfg"))
                if (img == null) img = ImageUtils.getDefaultIcon(nr)
                /*var title = "Profile $nr"
                val sniper = SnipSniper.getProfile(nr)
                if (sniper != null) title = sniper.getTitle()
                profiles.add(DropdownItem(title, file.name, img))*/
                var title = "Profile $nr"
                SnipSniper.getProfile(nr)?.also { profile -> title = profile.getTitle() }
                profiles.add(DropdownItem(title, file.name, img))
            }
        }
        if (configOriginal == null) profiles.add(0, DropdownItem("Select a profile", "select_profile"))
        val items = Array(profiles.size) { i -> profiles[i] }
        val dropdown = JComboBox(items)
        dropdown.renderer = DropdownItemRenderer(items)
        if (configOriginal == null) dropdown.setSelectedIndex(0) else activeDropdownIndex = DropdownItem.setSelected(dropdown, config.getFilename())
        val dropdownListener = arrayOf<ItemListener?>(null)
        dropdownListener[0] = ItemListener { e: ItemEvent ->
            if (e.stateChange == ItemEvent.SELECTED) {
                val requestedItem = dropdown.selectedIndex
                if (tabs[activeTabIndex]!!.isDirty) {
                    dropdown.removeItemListener(dropdownListener[0])
                    dropdown.selectedIndex = activeDropdownIndex
                    dropdown.addItemListener(dropdownListener[0])
                    if (showDirtyWarning() == JOptionPane.NO_OPTION) return@ItemListener
                    tabs[activeTabIndex]!!.isDirty = false
                    dropdown.selectedIndex = requestedItem
                }
                parentPanel.removeAll()
                val newConfig = Config((e.item as DropdownItem).id, "profile_defaults.cfg")
                setupPaneDynamic(newConfig, page)
                lastSelectedConfig = newConfig
                activeDropdownIndex = dropdown.selectedIndex
            }
        }
        dropdown.addItemListener(dropdownListener[0])
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.gridx = 0
        gbc.gridwidth = 2
        gbc.weightx = 1.0
        panelToAdd.add(dropdown, gbc)
        gbc.gridx = 2
        val profilePlusMinus = JPanel(GridLayout(0, 2))
        val profileAddButton = JButton("+")
        if (SnipSniper.getProfileCount() == SnipSniper.PROFILE_COUNT) profileAddButton.isEnabled = false
        profileAddButton.addActionListener {
            if (tabs[activeTabIndex]!!.isDirty) {
                val result = showDirtyWarning()
                if (result == JOptionPane.NO_OPTION) {
                    return@addActionListener
                }
            }
            for (i in 0 until SnipSniper.PROFILE_COUNT) {
                if (SnipSniper.getProfile(i) == null) {
                    SnipSniper.setProfile(i, Sniper(i))
                    val newProfileConfig = SnipSniper.getProfile(i)!!.config
                    newProfileConfig.save()
                    refreshConfigFiles()
                    parentPanel.removeAll()
                    generalTab.setup(newProfileConfig)
                    editorTab.setup(newProfileConfig)
                    viewerTab.setup(newProfileConfig)
                    lastSelectedConfig = newProfileConfig
                    break
                }
            }
        }
        profilePlusMinus.add(profileAddButton)
        val profileRemoveButton = JButton("-")
        val selectedItem = dropdown.selectedItem as DropdownItem?
        if (selectedItem != null)
            if (selectedItem.id.contains("profile0") || selectedItem.id.contains("editor")) profileRemoveButton.isEnabled = false
        profileRemoveButton.addActionListener {
            //No dirty check needs to be performed, we are deleting it anyways
            val item = dropdown.selectedItem as DropdownItem
            if (!item.id.contains("profile0") || !item.id.contains("editor")) {
                config.deleteFile()
                SnipSniper.resetProfiles()
                refreshConfigFiles()
                parentPanel.removeAll()
                var newIndex = dropdown.selectedIndex - 1
                if (newIndex < 0) newIndex = dropdown.selectedIndex + 1
                val newConfig = Config(dropdown.getItemAt(newIndex)!!.id, "profile_defaults.cfg")
                generalTab.setup(newConfig)
                editorTab.setup(newConfig)
                viewerTab.setup(newConfig)
                lastSelectedConfig = newConfig
            }
        }
        profilePlusMinus.add(profileRemoveButton)
        panelToAdd.add(profilePlusMinus, gbc)
        return dropdown
    }

    //Returns function you can run to update the state
    fun setupSaveButtons(panel: JPanel, tab: ITab, gbc: GridBagConstraints, config: Config, configOriginal: Config?, beforeSave: (() -> (Unit))?, reloadOtherDropdowns: Boolean): Function {
        val allowSaving = booleanArrayOf(true)
        val isDirty = booleanArrayOf(false)
        val save = JButton("config_label_save".translate())
        save.addActionListener {
            if (allowSaving[0] && configOriginal != null) {
                beforeSave?.invoke()
                configOriginal.loadFromConfig(config)
                configOriginal.save()
                SnipSniper.resetProfiles()
                if (reloadOtherDropdowns) {
                    generalTab.setup(configOriginal)
                    editorTab.setup(configOriginal)
                    viewerTab.setup(configOriginal)
                }
                //This prevents larger stamp settings leaving artifacts when saving and re-setting the tabs & panels
                editorTab.repaint()
            }
        }
        val close = JButton("config_label_close".translate())
        close.addActionListener {
            if (isDirty[0])
                if (showDirtyWarning() == JOptionPane.NO_OPTION) return@addActionListener
            close()
        }
        val setState: Function = object : Function() {
            override fun run(state: ConfigSaveButtonState): Boolean {
                if (configOriginal == null) return false
                when (state) {
                    ConfigSaveButtonState.UPDATE_CLEAN_STATE -> {
                        isDirty[0] = !config.settingsEquals(configOriginal)
                        tab.isDirty = isDirty[0]
                    }

                    ConfigSaveButtonState.YES_SAVE -> allowSaving[0] = true
                    ConfigSaveButtonState.NO_SAVE -> allowSaving[0] = false
                }
                if (isDirty[0])
                    close.text = "config_label_cancel".translate() else close.text = "config_label_close".translate()
                return true
            }
        }
        gbc.insets.top = 20
        gbc.gridx = 0
        panel.add(save, gbc)
        gbc.gridx = 1
        panel.add(close, gbc)
        return setState
    }

    private fun showDirtyWarning() = Utils.showPopup(this, "Unsaved changes, are you sure you want to cancel?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, "icons/redx.png".getImage(), true)

    fun getIDFromFilename(name: String): Int {
        val idString = name.replace(Config.DOT_EXTENSION, "").replace("profile", "")
        (idString.toIntOrNull() ?: -1).also { result ->
            if(result == -1) CCLogger.error("Issue parsing Filename to id: $name")
            return result
        }
    }

    fun setupColorButton(title: String?, config: Config, configKey: PROFILE?, whenChange: ChangeListener): GradientJButton {
        val startColorPBR = CCColor.fromSaveString(config.getString(configKey!!))
        val colorButton = GradientJButton(title!!, startColorPBR)
        startColorPBR.addChangeListener { config.set(configKey, startColorPBR.toSaveString()) }
        startColorPBR.addChangeListener(whenChange)
        colorButton.addActionListener {
            cWindows.add(CCColorChooser(startColorPBR, "Stamp color", this, useGradient = true))
        }
        return colorButton
    }

    fun setEnabledAll(component: JComponent, enabled: Boolean, vararg ignore: JComponent) {
        setEnableSpecific(component, enabled, *ignore)
        for (c in component.components) {
            if (c is JComponent) {
                setEnableSpecific(c, enabled, *ignore)
                if (c.components.isNotEmpty()) setEnabledAll(c, enabled, *ignore)
            }
        }
    }

    private fun setEnableSpecific(component: JComponent, enabled: Boolean, vararg ignore: JComponent) {
        var doDisable = true
        for (comp in ignore) if (comp === component) {
            doDisable = false
            break
        }
        if (doDisable) component.isEnabled = enabled
    }

    fun createJLabel(title: String?, horizontalAlignment: Int, verticalAlignment: Int): JLabel {
        val jlabel = JLabel(title)
        jlabel.horizontalAlignment = horizontalAlignment
        jlabel.verticalAlignment = verticalAlignment
        return jlabel
    }

    fun getGridLayoutWithMargin(row: Int, cols: Int, hGap: Int): GridLayout {
        val layout = GridLayout(row, cols)
        layout.hgap = hGap
        return layout
    }

    override fun close() {
        for (listener in listeners) listener.windowClosed()
        for (wnd in cWindows) wnd.close()
        dispose()
    }

    fun addCWindow(cWindow: CCIClosable) {
        cWindows.add(cWindow)
    }

    fun addCustomWindowListener(listener: CustomWindowListener) {
        listeners.add(listener)
    }

    companion object {
        fun generateScrollPane(component: JComponent?): JScrollPane {
            val scrollPane = JScrollPane(component)
            scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
            scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
            scrollPane.border = BorderFactory.createEmptyBorder()
            scrollPane.verticalScrollBar.unitIncrement = 20
            return scrollPane
        }
    }
}