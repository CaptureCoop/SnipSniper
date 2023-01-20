package net.snipsniper.sceditor

import net.snipsniper.SnipSniper
import net.snipsniper.StatsManager
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.sceditor.ezmode.EzModeSettingsCreator
import net.snipsniper.sceditor.ezmode.EzModeStampTab
import net.snipsniper.sceditor.stamps.StampType
import net.snipsniper.snipscope.SnipScopeWindow
import net.snipsniper.utils.*
import org.capturecoop.cccolorutils.chooser.CCColorChooser
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCIClosable
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList
import javax.imageio.ImageIO
import javax.swing.*

class SCEditorWindow(startImage: BufferedImage?, x: Int, y: Int, private var initialTitle: String, config: Config, isLeftToRight: Boolean, saveLocation: String?, inClipboard: Boolean, isStandalone: Boolean) : SnipScopeWindow(), CCIClosable {
    val config: Config
    var saveLocation: String?
    var inClipboard: Boolean
    var originalImage: BufferedImage
        private set
    val historyManager = SCEditorHistory(this)
    val stamps = Array(StampType.size) { i -> StampType.getByIndex(i).getIStamp(config, this) }
    private var selectedStamp = 0
    private val listener: SCEditorListener?
    private val renderer: SCEditorRenderer
    var isDirty = false
        set(value) {
            field = value
            refreshTitle()
        }
    val qualityHints = Utils.getRenderingHints()
    private var defaultImage: BufferedImage? = null
    private val cWindows = CopyOnWriteArrayList<CCIClosable>()
    var isStampVisible = true
    var ezMode: Boolean = config.getBool(ConfigHelper.PROFILE.ezMode)
        set(value) {
            field = value
            resizeTrigger()
            if(value) updateEzUI()
        }
    private val ezModeSettingsCreator = EzModeSettingsCreator(this)
    val ezModeWidth = 200
    val ezModeHeight = 40
    private val ezModeStampPanel = JPanel()
    private val ezModeTitlePanel = JPanel()
    private val ezModeTitle = JLabel("Marker")
    private val ezModeStampSettingsPanel = JPanel()
    private val ezModeStampSettingsScrollPane: JScrollPane
    private val ezModeStampPanelTabs = JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)
    private val isStandalone: Boolean
    private var stampColorChooser: CCColorChooser? = null
    var historyWindow: SCEditorHistoryWindow? = null
    private var configWindow: ConfigWindow? = null

    init {
        this.config = config
        this.title = initialTitle
        this.saveLocation = saveLocation
        this.inClipboard = inClipboard
        this.isStandalone = isStandalone
        if (startImage != null) image = ImageUtils.ensureAlphaLayer(startImage)
        CCLogger.info("Creating new editor window...")
        StatsManager.incrementCount(StatsManager.EDITOR_STARTED_AMOUNT)
        if (startImage == null) {
            if (config.getBool(ConfigHelper.PROFILE.standaloneStartWithEmpty)) {
                Toolkit.getDefaultToolkit().screenSize.also { ss ->
                    val w = ss.width / 2
                    val h = ss.height / 2
                    image = ImageUtils.newBufferedImage(w, h) {
                        it.color = Color.WHITE
                        it.fillRect(0, 0, w, h)
                    }
                }
            } else {
                image = ImageUtils.getDragPasteImage("icons/editor.png".getImage(), "Drop image here or use CTRL + V to paste one!")
                defaultImage = image
            }
        }
        renderer = SCEditorRenderer(this)
        listener = SCEditorListener(this)
        originalImage = image.clone()
        init(image, renderer, listener)
        layout = null

        //Setting up stamp array and stamp ui buttons
        val ezIconType = if (SnipSniper.config.getString(ConfigHelper.MAIN.theme) == "dark") "white" else "black"
        stamps.forEach {
            it.type.also { type -> addEZModeStampButton(type.title, type.iconFile, ezIconType, type.index) }
        }
        val tabRects = arrayOfNulls<Rectangle>(ezModeStampPanelTabs.tabCount)
        //TODO: Make this dynamic if we ever allow resizing
        for (i in tabRects.indices) tabRects[i] = ezModeStampPanelTabs.ui.getTabBounds(ezModeStampPanelTabs, i)
        ezModeStampPanelTabs.addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                for (i in tabRects.indices) {
                    if (tabRects[i]!!.contains(e.point)) {
                        setEzModeTitle(stamps[i].type.title)
                        break
                    }
                }
            }
        })
        ezModeStampPanelTabs.addChangeListener {
            setSelectedStamp(ezModeStampPanelTabs.selectedIndex)
            requestFocus()
        }
        ezModeStampPanel.layout = null
        ezModeStampPanel.add(ezModeStampPanelTabs)
        ezModeTitle.horizontalAlignment = JLabel.CENTER
        ezModeTitle.verticalAlignment = JLabel.CENTER
        ezModeTitlePanel.add(ezModeTitle)
        ezModeSettingsCreator.addSettingsToPanel(ezModeStampSettingsPanel, getSelectedStamp(), ezModeWidth)
        ezModeStampSettingsScrollPane = JScrollPane(ezModeStampSettingsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER)
        ezModeStampSettingsScrollPane.border = BorderFactory.createEmptyBorder()
        ezModeStampSettingsScrollPane.verticalScrollBar.unitIncrement = 10
        ezModeStampSettingsScrollPane.isWheelScrollingEnabled = true
        add(ezModeStampPanel)
        add(ezModeStampSettingsScrollPane)
        add(ezModeTitlePanel)
        historyManager.resetHistory()
        iconImage = "icons/editor.png".getImage()
        focusTraversalKeysEnabled = false
        isVisible = true
        if (!(x < 0 && y < 0)) {
            var borderSize = config.getInt(ConfigHelper.PROFILE.borderSize)
            if (!isLeftToRight) borderSize = -borderSize
            setLocation(x - X_OFFSET + borderSize, y - insets.top + borderSize)
            CCLogger.info("Setting location to $location")
        }
        refreshTitle()
        setSizeAuto()
        if (x < 0 && y < 0) setLocationAuto()
        //Menu bar
        kotlin.run {
            val topBar = JMenuBar()
            fun sizeImage(path: String) = path.getImage().scaled(16, 16).toImageIcon()
            fun ctrlStroke(keyCode: Int) = KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK)
            fun altStroke(keyCode: Int) = KeyStroke.getKeyStroke(keyCode, InputEvent.ALT_DOWN_MASK)
            val devString = "Disabled menu items are still in development."

            JMenu("File").also { parent ->
                parent.icon = sizeImage("icons/folder.png")
                JMenuItem("New").also {
                    it.icon = sizeImage("icons/questionmark.png")
                    it.accelerator = ctrlStroke(KeyEvent.VK_N)
                    it.addActionListener { openNewImageWindow() }
                    parent.add(it)
                }
                JMenuItem("Open").also {
                    it.icon = sizeImage("icons/questionmark.png")
                    it.addActionListener {
                        JFileChooser().also { fc ->
                            if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                                setImage(ImageIO.read(fc.selectedFile), resetHistory = true, isNewImage = true)
                        }
                    }
                    parent.add(it)
                }
                JMenuItem("Save").also {
                    it.icon = sizeImage("icons/questionmark.png")
                    it.accelerator = ctrlStroke(KeyEvent.VK_S)
                    it.addActionListener { save(false) }
                    parent.add(it)
                }
                JMenuItem("Save and close").also {
                    it.icon = sizeImage("icons/questionmark.png")
                    it.accelerator = altStroke(KeyEvent.VK_S)
                    it.addActionListener { save(true) }
                    parent.add(it)
                }
                parent.addSeparator()
                JMenuItem("Close").also { closeItem ->
                    closeItem.icon = sizeImage("icons/redx.png")
                    closeItem.accelerator = KeyStroke.getKeyStroke("ESCAPE")
                    closeItem.addActionListener { close() }
                    parent.add(closeItem)
                }
                topBar.add(parent)
            }
            JMenu("Edit").also { parent ->
                parent.icon = sizeImage("icons/editor.png")
                JMenuItem("Config").also {
                    it.icon = sizeImage("icons/config.png")
                    it.addActionListener { openConfigWindow() }
                    parent.add(it)
                }
                parent.addSeparator()
                JMenuItem("Undo").also {
                    it.icon = sizeImage("icons/restart.png")
                    it.accelerator = ctrlStroke(KeyEvent.VK_Z)
                    it.addActionListener { undo() }
                    parent.add(it)
                }
                JMenuItem("Redo").also {
                    it.icon = ImageUtils.imageToBufferedImage(sizeImage("icons/restart.png").image).flipHorizontally().toImageIcon()
                    it.accelerator = ctrlStroke(KeyEvent.VK_R)
                    it.isEnabled = false
                    it.toolTipText = devString
                    parent.add(it)
                }
                parent.addSeparator()
                JMenuItem("Copy").also {
                    it.icon = sizeImage("icons/copy.png")
                    it.accelerator = ctrlStroke(KeyEvent.VK_C)
                    it.addActionListener { image.copyToClipboard() }
                    parent.add(it)
                }
                JMenuItem("Paste").also {
                    it.icon = sizeImage("icons/paste.png")
                    it.accelerator = ctrlStroke(KeyEvent.VK_V)
                    it.addActionListener { doPaste() }
                    parent.add(it)
                }
                parent.addSeparator()
                JMenuItem("Flip horizontally").also {
                    it.icon = sizeImage("icons/mirror_horizontal.png")
                    it.addActionListener {
                        setImage(image.flipHorizontally(), resetHistory = false, isNewImage = false)
                        historyManager.addHistory()
                    }
                    parent.add(it)
                }
                JMenuItem("Flip vertically").also {
                    it.icon = sizeImage("icons/mirror_vertical.png")
                    it.addActionListener {
                        setImage(image.flipVertically(), resetHistory = false, isNewImage = false)
                        historyManager.addHistory()
                    }
                    parent.add(it)
                }
                JMenuItem("Resize").also {
                    it.icon = sizeImage("icons/resize.png")
                    var wnd: ResizeWindow? = null
                    it.addActionListener {
                        if(wnd != null) {
                            wnd?.requestFocus()
                        } else {
                            wnd = ResizeWindow(image, parent = this).also { cWindows.add(it) }
                            wnd?.onClose = { wnd = null }
                            wnd?.onSubmit = {
                                println("Resized: $image")
                            }
                        }
                    }
                    parent.add(it)
                }
                parent.addSeparator()
                JMenuItem("History").also {
                    it.icon = sizeImage("icons/clock.png")
                    it.addActionListener { openHistoryWindow() }
                    parent.add(it)
                }
                topBar.add(parent)
            }
            JMenu("Stamps").also { parent->
                parent.icon = sizeImage("icons/stamp.png")
                for(i in 0 until StampType.size) {
                    StampType.getByIndex(i).also { stamp ->
                        JMenuItem(stamp.title).also {
                            it.icon = sizeImage("icons/stamp.png")
                            it.accelerator = KeyStroke.getKeyStroke((i + 1).toString())
                            it.addActionListener { setSelectedStamp(i) }
                            parent.add(it)
                        }
                    }
                }
                topBar.add(parent)
            }
            JMenu("Experimental").also { parent ->
                parent.icon = sizeImage("icons/debug.png")
                JMenuItem("Border test").also {
                    it.icon = parent.icon
                    it.addActionListener {
                        val borderThickness = 10
                        //Fix to have this work without originalImage. As we will remove/Change this anyway I don't care if this affects anything for now.
                        ImageUtils.newBufferedImage(image.width + borderThickness, image.height + borderThickness) { g ->
                            g as Graphics2D
                            g.setRenderingHints(qualityHints)
                            for (y1 in 0 until image.height) {
                                for (x1 in 0 until image.width) {
                                    if (Color(image.getRGB(x1, y1), true).alpha > 10) {
                                        g.color = Color.WHITE
                                        g.fillOval(x1 + borderThickness / 2 - borderThickness / 2, y1 + borderThickness / 2 - borderThickness / 2, borderThickness, borderThickness)
                                    }
                                }
                            }
                            g.drawImage(image, borderThickness / 2, borderThickness / 2, image.width, image.height, null)
                        }.also { img ->
                            setImage(img, resetHistory = true, isNewImage = true)
                        }
                        isDirty = true
                        repaint()
                        refreshTitle()
                    }
                    parent.add(it)
                }
                JMenuItem("Box test").also {
                    it.icon = parent.icon
                    it.addActionListener {
                        val width = 512
                        val height = 512
                        val test = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                        val g = test.graphics as Graphics2D
                        g.setRenderingHints(qualityHints)
                        val optimalDimension = Utils.getScaledDimension(originalImage, Dimension(width, height))
                        g.drawImage(originalImage, test.width / 2 - optimalDimension.width / 2, test.height / 2 - optimalDimension.height / 2, optimalDimension.width, optimalDimension.height, null)
                        g.dispose()
                        setImage(test, resetHistory = true, isNewImage = true)
                        isDirty = true
                        repaint()
                        refreshTitle()
                    }
                    parent.add(it)
                }
                topBar.add(parent)
            }
            jMenuBar = topBar
        }
        if (ezMode) {
            setSize(width + ezModeWidth, height + ezModeHeight)
            setLocation(location.x - ezModeWidth, location.y - ezModeHeight)
        }
        if (!isStandalone) {
            val localGE = GraphicsEnvironment.getLocalGraphicsEnvironment()
            var found = false
            var bestMonitor: GraphicsConfiguration? = null
            //This prevents this setup not working if you do a screenshot on the top left, which would cause the location not to be in any bounds
            val safetyOffsetX = 10 + config.getInt(ConfigHelper.PROFILE.borderSize)
            for (gd in localGE.screenDevices) {
                for (graphicsConfiguration in gd.configurations) {
                    if (!found) {
                        val bounds = graphicsConfiguration.bounds
                        val testLocation = Point(location.x + safetyOffsetX, location.y)
                        if (bounds.contains(testLocation)) found = true
                        if (testLocation.getX() > bounds.getX() && testLocation.getX() < bounds.getX() + bounds.getWidth() && bestMonitor == null) {
                            bestMonitor = graphicsConfiguration
                        }
                    }
                }
            }
            if (!found && bestMonitor != null) {
                setLocation(location.x, bestMonitor.bounds.y)
            }
        }
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                super.windowClosing(e)
                close()
            }
        })
        isEnableInteraction = !isDefaultImage()
        requestFocus()
        CCLogger.info("Started new editor window. ($this)")
    }

    private fun addEZModeStampButton(title: String?, iconName: String?, theme: String?, stampIndex: Int) {
        ezModeStampPanelTabs.addTab(title, null)
        "ui/editor/$theme/$iconName.png".getImage().also { ezIconMarker ->
            ezModeStampPanelTabs.setTabComponentAt(stampIndex, EzModeStampTab(ezIconMarker, 32, this, stampIndex))
            ezModeStampPanelTabs.setIconAt(stampIndex, ImageIcon(ezIconMarker))
        }
    }

    override fun resizeTrigger() {
        super.resizeTrigger()
        if (ezMode) {
            val titleMargin = 5
            var ezModeWidthToUse = ezModeWidth
            if (ezModeStampSettingsScrollPane.verticalScrollBar.isVisible) ezModeWidthToUse += ezModeStampSettingsScrollPane.verticalScrollBar.width
            ezModeTitlePanel.setBounds(0, 0, ezModeWidthToUse, ezModeHeight)
            ezModeTitle.font = Font("Arial", Font.PLAIN, ezModeHeight - titleMargin)
            ezModeStampPanel.setBounds(ezModeWidthToUse, 0, contentPane.width - ezModeWidthToUse, ezModeHeight)
            ezModeStampPanelTabs.setBounds(0, 0, ezModeStampPanel.width, ezModeStampPanel.height)
            Dimension(ezModeWidthToUse, ezModeSettingsCreator.lastCorrectHeight()).also { dim ->
                ezModeStampSettingsPanel.preferredSize = dim
                ezModeStampSettingsPanel.minimumSize = dim
                ezModeStampSettingsPanel.maximumSize = dim
            }
            ezModeStampSettingsScrollPane.setBounds(0, ezModeHeight, ezModeWidthToUse, contentPane.height - ezModeHeight)
            renderer.setBounds(ezModeWidthToUse, ezModeHeight, contentPane.width - ezModeWidthToUse, contentPane.height - ezModeHeight)
        } else {
            ezModeTitlePanel.setBounds(0, 0, 0, 0)
            ezModeStampPanel.setBounds(0, 0, 0, 0)
            renderer.setBounds(0, 0, contentPane.width, contentPane.height)
            requestFocus()
        }
    }

    private fun openNewImageWindow() {
        val window = NewImageWindow()
        cWindows.add(window)
        window.onSubmit = {
            setImage(window.image, resetHistory = true, isNewImage = true)
            isDirty = true
            repaint()
            refreshTitle()
        }
    }

    fun save(close: Boolean = false) {
        CCLogger.info("Saving editor, close=$close")
        if(isDirty) {
            val location = ImageUtils.saveImage(image, config.getString(ConfigHelper.PROFILE.saveFormat), FILENAME_MODIFIER, config)
            location?.replace(File(location).name, "")?.also { loc ->
                config.set(ConfigHelper.PROFILE.lastSaveFolder, loc)
                config.save()
            }
            if (config.getBool(ConfigHelper.PROFILE.copyToClipboard)) image.copyToClipboard()
            isDirty = false
        }
        if(close) close()
    }

    fun refreshTitle() {
        CCLogger.debug("Refreshing title")
        var newTitle: String? = initialTitle

        //Path
        if (saveLocation != null && saveLocation!!.isNotEmpty()) {
            newTitle += when(config.getBool(ConfigHelper.PROFILE.fullPathInEditorTitle)) {
                true -> " ($saveLocation)"
                false -> " (${File(saveLocation!!).name})"
            }
        }

        //Clipboard
        if (inClipboard) newTitle += " (Clipboard)"

        //Size
        newTitle += " ${image.width}x${image.height}"

        //Dirty
        if(isDirty) newTitle += "*"
        
        title = newTitle
    }

    fun setImage(newImage: BufferedImage?, resetHistory: Boolean, isNewImage: Boolean) {
        super.image = ImageUtils.ensureAlphaLayer(newImage!!)
        CCLogger.debug("Setting new Image")
        isEnableInteraction = !isDefaultImage()
        if (listener != null && resetHistory) {
            historyManager.resetHistory()
            for (stamp in stamps) stamp.reset()
        }
        if (isNewImage) {
            resetZoom()
            renderer.resetPreview()
            originalImage = image.clone()
        }
        repaint()
    }

    fun getSelectedStamp() = stamps[selectedStamp]

    fun setEzModeTitle(title: String?) = kotlin.run { ezModeTitle.text = title }

    fun setSelectedStamp(i: Int) {
        if (selectedStamp == i) return
        selectedStamp = i
        ezModeStampPanelTabs.selectedIndex = i
        setEzModeTitle(getSelectedStamp().type.title)
        updateEzUI()
        openStampColorChooser(true)
    }

    private fun updateEzUI() = when(ezMode) {
        true -> ezModeSettingsCreator.addSettingsToPanel(ezModeStampSettingsPanel, getSelectedStamp(), ezModeWidth)
        false -> ezModeStampSettingsPanel.removeAll()
    }

    private fun doPaste() {
        if(isDirty) {
            JOptionPane.showConfirmDialog(this, "Changes present, are you sure you want to replace the current image?", "Warning", JOptionPane.YES_NO_OPTION).also {
                if(it == 1) return
            }
        }
        ImageUtils.getImageFromClipboard().also {
            if(it == null) return
            saveLocation = ""
            inClipboard = true
            setImage(it, resetHistory = true, isNewImage = true)
            isDirty = false
            refreshTitle()
        }
    }

    override fun toString() = "SCEditorWindow Pos:[$location] Path:[$saveLocation]"

    fun isDefaultImage() = defaultImage === image

    override fun close() {
        if(isDirty) {
            JOptionPane.showConfirmDialog(this, "Changes present, are you sure you want to exit?", "Warning", JOptionPane.YES_NO_OPTION).also {
                if(it == 1) return
            }
        }
        cWindows.forEach { it.close() }
        dispose()
        if (isStandalone) SnipSniper.exit(false)
        dispose()
    }

    //This opens a color chooser for the stamp reliably, making sure not to open more than one and to update
    //The chooser if needed (verifyOnly -> Don't create one even if its null)
    fun openStampColorChooser(verifyOnly: Boolean = false) {
        val stamp = stamps[selectedStamp]
        val title = "${stamp.type.title} color"
        if(stampColorChooser == null && !verifyOnly) {
            CCColorChooser(stamp.color!!, title, parent = this, useGradient = true, backgroundImage = originalImage).also { chooser ->
                stampColorChooser = chooser
                cWindows.add(chooser)
                chooser.setOnClose {
                    cWindows.remove(chooser)
                    stampColorChooser = null
                }
            }
        } else {
            if(stampColorChooser == null) return

            stampColorChooser.also {
                if(stamp.color == null) {
                    it?.close()
                    return
                }
                it!!.title = title
                it.color = stamp.color!!
                it.requestFocus()
            }
        }
    }

    private fun undo() = historyManager.undoHistory()

    private fun openConfigWindow() {
        if(configWindow == null) {
            configWindow = ConfigWindow(config, ConfigWindow.PAGE.EditorPanel).also { cfgWnd ->
                cWindows.add(cfgWnd)
                cfgWnd.addCustomWindowListener {
                    //Config window is closing by itself, remove it from the listeners and its singleton reference
                    configWindow = null
                    cWindows.remove(cfgWnd)
                }
            }
        } else configWindow!!.requestFocus()
    }

    private fun openHistoryWindow() {
        if(historyWindow != null) {
            historyWindow!!.requestFocus()
            return
        }
        SCEditorHistoryWindow(this).also { newWnd ->
            cWindows.add(newWnd)
            newWnd.setOnClose {
                cWindows.remove(newWnd)
                historyWindow = null
            }
            historyWindow = newWnd
        }
    }

    companion object {
        private const val X_OFFSET = 8
        const val FILENAME_MODIFIER = "_edited"
        val standaloneEditorConfig: Config
            get() = Config("editor.cfg", "profile_defaults.cfg")
    }
}