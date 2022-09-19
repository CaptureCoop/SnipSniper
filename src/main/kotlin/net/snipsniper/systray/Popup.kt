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
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class Popup(private val sniper: Sniper): JFrame() {
    private val taskbarHeight = 40

    init {
        val config = sniper.config
        isUndecorated = true
        rootPane.border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK)
        layout = BoxLayout(contentPane, BoxLayout.PAGE_AXIS)
        ImageManager.getImage("splash.png").also { splash ->
            val w = (splash.width / 3F).toInt()
            val h = (splash.height / 3F).toInt()
            JLabel(ImageIcon(splash.getScaledInstance(w, h, Image.SCALE_SMOOTH))).also {
                it.text = sniper.getTitle()
                it.alignmentX = JPanel.CENTER_ALIGNMENT
                it.verticalTextPosition = JLabel.BOTTOM
                it.horizontalTextPosition = JLabel.CENTER
                add(it)
            }
        }

        val menus = ArrayList<PopupMenu>()
        add(PopupMenuButton("Viewer", "icons/viewer.png", this, { SCViewerWindow(null, config, false) }, menus))
        add(PopupMenuButton("Editor", "icons/editor.png", this, { SCEditorWindow(null, -1, -1, "SnipSniper Editor", config, true, null, false, false) }, menus))
        add(JSeparator())
        add(PopupMenuButton(LangManager.getItem("menu_open_image_folder"), "icons/folder.png", this, {
            config.getString(ConfigHelper.PROFILE.lastSaveFolder).also { lsf ->
                if(lsf.isEmpty() || lsf == "none" || !FileUtils.exists(lsf))
                    FileUtils.openFolder(config.getString(ConfigHelper.PROFILE.pictureFolder))
                else FileUtils.openFolder(lsf)
            }
        }, menus))
        add(PopupMenuButton(LangManager.getItem("menu_config"), "icons/config.png", this, { SnipSniper.openConfigWindow(sniper) }, menus))

        if(SnipSniper.isDebug()) {
            PopupMenu("Debug", ImageManager.getImage("icons/debug.png")).also { pm ->
                pm.add(PopupMenuButton("Console", "icons/console.png", this, { CCLogger.enableDebugConsole(true) }, menus))
                pm.add(PopupMenuButton("Open log folder", "icons/folder.png", this, { FileUtils.openFolder(SnipSniper.logFolder) }, menus))
                pm.add(PopupMenuButton("Language test", "icons/config.png", this, { LangDebugWindow() }, menus))
                add(pm).also { menus.add(pm) }
            }
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
        val gc = Utils.getGraphicsConfiguration(x, y)
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