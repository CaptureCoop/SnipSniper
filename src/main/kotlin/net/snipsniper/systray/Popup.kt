package net.snipsniper.systray

import net.snipsniper.ImageManager
import net.snipsniper.LangManager
import net.snipsniper.SnipSniper
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.scviewer.SCViewerWindow
import net.snipsniper.utils.AboutWindow
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.Utils
import net.snipsniper.utils.debug.LangDebugWindow
import org.capturecoop.cclogger.CCLogger
import java.awt.Color
import java.awt.Image
import java.awt.Point
import java.awt.Toolkit
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import java.io.File
import javax.swing.*

class Popup(private val sniper: Sniper): JFrame() {
    private val taskbarHeight = 40

    init {
        val config = sniper.config
        isUndecorated = true
        rootPane.border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK)
        layout = BoxLayout(contentPane, BoxLayout.PAGE_AXIS)
        val splash = ImageManager.getImage("splash.png")
        val title = JLabel(ImageIcon(splash.getScaledInstance((splash.width / 3F).toInt(), (splash.height / 3F).toInt(), Image.SCALE_SMOOTH)))
        title.text = sniper.title
        title.alignmentX = JPanel.CENTER_ALIGNMENT
        title.verticalTextPosition = JLabel.BOTTOM
        title.horizontalTextPosition = JLabel.CENTER
        add(title)

        val menus = ArrayList<PopupMenu>()
        add(PopupMenuButton("Viewer", "icons/viewer.png", this, { SCViewerWindow(null, config, false) }, menus))
        add(PopupMenuButton("Editor", "icons/editor.png", this, { SCEditorWindow(null, -1, -1, "SnipSniper Editor", config, true, null, false, false) }, menus))
        add(JSeparator())
        add(PopupMenuButton(LangManager.getItem("menu_open_image_folder"), "icons/folder.png", this, {
            var folderToOpen = sniper.config.getString(ConfigHelper.PROFILE.lastSaveFolder)
            if(folderToOpen.isEmpty() || folderToOpen == "none" || !File(folderToOpen).exists())
                folderToOpen = sniper.config.getString(ConfigHelper.PROFILE.pictureFolder)
            FileUtils.openFolder(folderToOpen)
        }, menus))
        add(PopupMenuButton(LangManager.getItem("menu_config"), "icons/config.png", this, { SnipSniper.openConfigWindow(sniper) }, menus))

        if(SnipSniper.isDebug()) {
            val debugMenu = PopupMenu("Debug", ImageManager.getImage("icons/debug.png"))
            debugMenu.add(PopupMenuButton("Console", "icons/console.png", this, { CCLogger.enableDebugConsole(true) }, menus))
            debugMenu.add(PopupMenuButton("Open log folder", "icons/folder.png", this, { FileUtils.openFolder(SnipSniper.logFolder) }, menus))
            debugMenu.add(PopupMenuButton("Language test", "icons/config.png", this, { LangDebugWindow() }, menus))
            add(debugMenu)
            menus.add(debugMenu)
        }

        add(PopupMenuButton(LangManager.getItem("menu_about"), "icons/about.png", this, { AboutWindow(sniper) }, menus))
        add(JSeparator())
        add(PopupMenuButton("Restart", "icons/redx.png", this, { SnipSniper.restart() }, menus))
        add(PopupMenuButton(LangManager.getItem("menu_quit"), "icons/redx.png", this, { SnipSniper.exit(false) }, menus))

        iconImage = ImageManager.getImage("icons/snipsniper.png")
        addFocusListener(object: FocusAdapter() {
            override fun focusLost(e: FocusEvent?) {
                super.focusLost(e)
                isVisible = false
            }
        })
    }

    fun showPopup(x: Int, y: Int) {
        isVisible = true
        pack()

        //We do this in order to know which monitor the mouse position is on, before actually placing the popup jframe
        val testGC = JFrame()
        testGC.isUndecorated = true
        testGC.location = Point(x, y)
        testGC.isVisible = true
        val gc = testGC.graphicsConfiguration
        testGC.dispose()

        val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
        val screenRect = gc.bounds

        if(screenRect.x != 0 || screenRect.y != 0) {
            //This currently only allows non-default screens to work if taskbar is on bottom. Find better way!!
            //TODO: ^^^^^^^^^^^^^^^^^^
            //IDEA: Take half of the screens width to determine if we are left right bottom or top and then calculate position based on that, if possible
            setLocation(getX(), getY() - height - insets.bottom)
            if(!Utils.containsRectangleFully(screenRect, bounds)) {
                //Fallback
                //TODO: Find prettier way
                setLocation(screenRect.width / 2 - width / 2, screenRect.height / 2 - height / 2)
            }
        } else {
            if (insets.bottom != 0)
                setLocation(x, screenRect.height - height - insets.bottom)
            else if (insets.top != 0)
                setLocation(x, insets.top)
            else if (insets.left != 0)
                setLocation(insets.left, y - height)
            else if (insets.right != 0)
                setLocation(screenRect.width - width - insets.right, y - height)
            else
                setLocation(x, screenRect.height - height - taskbarHeight)
            /* If "Let taskbar scroll down when not in use" is enabled insets is all 0, use 40 for now, should work fine */
        }
        requestFocus()
    }
}