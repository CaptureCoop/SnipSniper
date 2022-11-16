package net.snipsniper.sceditor

import net.snipsniper.ImageManager.Companion.getImage
import net.snipsniper.SnipSniper
import net.snipsniper.SnipSniper.Companion.exit
import net.snipsniper.SnipSniper.Companion.openConfigWindow
import net.snipsniper.StatsManager
import net.snipsniper.StatsManager.Companion.incrementCount
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.ezmode.EzModeSettingsCreator
import net.snipsniper.sceditor.ezmode.EzModeStampTab
import net.snipsniper.sceditor.stamps.IStamp
import net.snipsniper.sceditor.stamps.StampType
import net.snipsniper.snipscope.SnipScopeWindow
import net.snipsniper.utils.IFunction
import net.snipsniper.utils.ImageUtils.Companion.copyImage
import net.snipsniper.utils.ImageUtils.Companion.copyToClipboard
import net.snipsniper.utils.ImageUtils.Companion.ensureAlphaLayer
import net.snipsniper.utils.ImageUtils.Companion.getDragPasteImage
import net.snipsniper.utils.ImageUtils.Companion.saveImage
import net.snipsniper.utils.Utils
import net.snipsniper.utils.Utils.Companion.getScaledDimension
import net.snipsniper.utils.getImage
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.cclogger.CCLogger.Companion.info
import org.capturecoop.ccutils.utils.CCIClosable
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.*
import java.awt.event.*
import java.awt.image.BufferedImage
import java.io.File
import javax.swing.*

class SCEditorWindow(img: BufferedImage?, x: Int, y: Int, title: String, config: Config, isLeftToRight: Boolean, saveLocation: String?, inClipboard: Boolean, isStandalone: Boolean) : SnipScopeWindow(), CCIClosable {
    val config: Config
    var saveLocation: String?
    var inClipboard: Boolean
    var originalImage: BufferedImage
        private set
    val stamps = Array(StampType.size) { i -> StampType.getByIndex(i).getIStamp(config, this) }
    private var selectedStamp = 0
    private val listener: SCEditorListener?
    private val renderer: SCEditorRenderer
    var isDirty = false
    val qualityHints = Utils.getRenderingHints()
    private var defaultImage: BufferedImage? = null
    private val cWindows = ArrayList<CCIClosable>()
    var isStampVisible = true
    var ezMode: Boolean = config.getBool(ConfigHelper.PROFILE.ezMode)
        set(value) {
            field = value
            resizeTrigger()
            updateEzUI(true)
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

    init {
        var image = img
        this.config = config
        this.title = title
        this.saveLocation = saveLocation
        this.inClipboard = inClipboard
        this.isStandalone = isStandalone
        if (image != null) image = ensureAlphaLayer(image)
        info("Creating new editor window...")
        incrementCount(StatsManager.EDITOR_STARTED_AMOUNT)
        if (image == null) {
            if (config.getBool(ConfigHelper.PROFILE.standaloneStartWithEmpty)) {
                val imgSize = Toolkit.getDefaultToolkit().screenSize
                image = BufferedImage(imgSize.width / 2, imgSize.height / 2, BufferedImage.TYPE_INT_RGB)
                val imgG = image.graphics
                imgG.color = Color.WHITE
                imgG.fillRect(0, 0, image.width, image.height)
                imgG.dispose()
            } else {
                image = getDragPasteImage(getImage("icons/editor.png"), "Drop image here or use CTRL + V to paste one!")
                defaultImage = image
            }
        }
        renderer = SCEditorRenderer(this)
        listener = SCEditorListener(this)
        originalImage = copyImage(image)
        init(image, renderer, listener)
        layout = null
        var ezIconType = "black"
        if (SnipSniper.config.getString(ConfigHelper.MAIN.theme) == "dark") {
            ezIconType = "white"
        }

        //Setting up stamp array and stamp ui buttons
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
        listener.resetHistory()
        iconImage = getImage("icons/editor.png")
        focusTraversalKeysEnabled = false
        isVisible = true
        if (!(x < 0 && y < 0)) {
            var borderSize = config.getInt(ConfigHelper.PROFILE.borderSize)
            if (!isLeftToRight) borderSize = -borderSize
            setLocation(x - X_OFFSET + borderSize, y - insets.top + borderSize)
            info("Setting location to $location")
        }
        refreshTitle()
        setSizeAuto()
        if (x < 0 && y < 0) setLocationAuto()
        if (SystemUtils.IS_OS_WINDOWS) {
            val topBar = JMenuBar()
            val configItem = JMenuItem("Config")
            configItem.addActionListener { openConfigWindow(this) }
            topBar.add(configItem)
            val newItem = JMenuItem("New")
            newItem.addActionListener { openNewImageWindow() }
            topBar.add(newItem)
            val whatsappTest = JMenuItem("Border test")
            whatsappTest.addActionListener {
                val borderThickness = 10
                //Fix to have this work without originalImage. As we will remove/Change this anyways i dont care if this affects anything for now.
                val imageToUse = image
                val test = BufferedImage(
                    imageToUse.width + borderThickness,
                    imageToUse.height + borderThickness,
                    BufferedImage.TYPE_INT_ARGB
                )
                val g = test.graphics as Graphics2D
                g.setRenderingHints(qualityHints)
                for (y1 in 0 until imageToUse.height) {
                    for (x1 in 0 until imageToUse.width) {
                        if (Color(imageToUse.getRGB(x1, y1), true).alpha > 10) {
                            g.color = Color.WHITE
                            g.fillOval(
                                x1 + borderThickness / 2 - borderThickness / 2,
                                y1 + borderThickness / 2 - borderThickness / 2,
                                borderThickness,
                                borderThickness
                            )
                        }
                    }
                }
                g.drawImage(
                    imageToUse,
                    borderThickness / 2,
                    borderThickness / 2,
                    imageToUse.width,
                    imageToUse.height,
                    null
                )
                g.dispose()
                setImage(test, true, true)
                isDirty = true
                repaint()
                refreshTitle()
            }
            topBar.add(whatsappTest)
            val whatsappBox = JMenuItem("Box test")
            whatsappBox.addActionListener {
                val width = 512
                val height = 512
                val test = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                val g = test.graphics as Graphics2D
                g.setRenderingHints(qualityHints)
                val optimalDimension = getScaledDimension(originalImage, Dimension(width, height))
                g.drawImage(
                    originalImage,
                    test.width / 2 - optimalDimension.width / 2,
                    test.height / 2 - optimalDimension.height / 2,
                    optimalDimension.width,
                    optimalDimension.height,
                    null
                )
                g.dispose()
                setImage(test, true, true)
                isDirty = true
                repaint()
                refreshTitle()
            }
            topBar.add(whatsappBox)
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
            val SAFETY_OFFSET_X =
                10 + config.getInt(ConfigHelper.PROFILE.borderSize) //This prevents this setup not working if you do a screenshot on the top left, which would cause the location not to be in any bounds
            for (gd in localGE.screenDevices) {
                for (graphicsConfiguration in gd.configurations) {
                    if (!found) {
                        val bounds = graphicsConfiguration.bounds
                        val testLocation = Point(location.x + SAFETY_OFFSET_X, location.y)
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
        info("Started new editor window. ($this)")
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

    fun openNewImageWindow() {
        val window = NewImageWindow()
        cWindows.add(window)
        val posX = location.x + width / 2 - window.width / 2
        val posY = location.y + height / 2 - window.height / 2
        window.setLocation(posX, posY)
        window.onSubmit = IFunction {
            setImage(window.image, true, true)
            isDirty = true
            repaint()
            refreshTitle()
        }
    }

    fun saveImage() {
        val location = saveImage(image, config.getString(ConfigHelper.PROFILE.saveFormat), FILENAME_MODIFIER, config)
        if (location != null) {
            val folder = location.replace(File(location).name, "")
            config.set(ConfigHelper.PROFILE.lastSaveFolder, folder)
            config.save()
        }
        if (config.getBool(ConfigHelper.PROFILE.copyToClipboard)) copyToClipboard(image)
    }

    fun refreshTitle() {
        info("Refreshing title")
        var newTitle: String? = title
        if (saveLocation != null && saveLocation!!.isNotEmpty()) newTitle += " ($saveLocation)"
        if (inClipboard) {
            newTitle += " (Clipboard)"
        }
        newTitle += " ${image.width}x${image.height}"
        title = newTitle
    }

    fun setImage(newImage: BufferedImage?, resetHistory: Boolean, isNewImage: Boolean) {
        super.image = ensureAlphaLayer(newImage!!)
        info("Setting new Image")
        isEnableInteraction = !isDefaultImage()
        if (listener != null && resetHistory) {
            listener.resetHistory()
            for (stamp in stamps) stamp.reset()
        }
        if (isNewImage) {
            resetZoom()
            renderer.resetPreview()
            originalImage = copyImage(image)
        }
    }

    fun getSelectedStamp() = stamps[selectedStamp]

    fun setEzModeTitle(title: String?) = kotlin.run { ezModeTitle.text = title }

    fun setSelectedStamp(i: Int) {
        if (selectedStamp == i) return
        selectedStamp = i
        ezModeStampPanelTabs.selectedIndex = i
        setEzModeTitle(getSelectedStamp().type.title)
        updateEzUI(true)
    }

    private fun updateEzUI(reset: Boolean) {
        if (ezMode && reset) ezModeSettingsCreator.addSettingsToPanel(
            ezModeStampSettingsPanel,
            getSelectedStamp(),
            ezModeWidth
        ) else if (!ezMode && reset) ezModeStampSettingsPanel.removeAll()
    }

    override fun toString() = "SCEditorWindow Pos:[$location] Path:[$saveLocation]"

    fun isDefaultImage() = defaultImage === image

    fun addClosableWindow(wnd: CCIClosable) = kotlin.run { cWindows.add(wnd) }

    override fun close() {
        cWindows.forEach { it.close() }
        dispose()
        if (isStandalone) exit(false)
    }

    companion object {
        private const val X_OFFSET = 8
        const val FILENAME_MODIFIER = "_edited"
        val standaloneEditorConfig: Config
            get() = Config("editor.cfg", "profile_defaults.cfg")
    }
}