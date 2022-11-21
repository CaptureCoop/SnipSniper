package net.snipsniper.systray

import net.snipsniper.SnipSniper
import net.snipsniper.config.ConfigHelper
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.scviewer.SCViewerWindow
import net.snipsniper.utils.*
import net.snipsniper.utils.debug.LangDebugWindow
import org.capturecoop.cclogger.CCLogger
import java.awt.*
import java.awt.event.FocusAdapter
import java.awt.event.FocusEvent
import javax.swing.*

class Popup(private val sniper: Sniper): JDialog() {

    init {
        val config = sniper.config
        isUndecorated = true
        rootPane.border = BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK)
        layout = BoxLayout(contentPane, BoxLayout.PAGE_AXIS)
        "splash.png".getImage().also { splash ->
            fun c(v: Int) = (v / 3F).toInt()
            JLabel(splash.scaledSmooth(c(splash.width), c(splash.height)).toImageIcon()).also {
                it.alignmentX = JPanel.CENTER_ALIGNMENT
                add(it)
            }
        }

        if(SnipSniper.buildInfo.releaseType != ReleaseType.STABLE) {
            JLabel(SnipSniper.getVersionString()).also {
                it.alignmentX = JPanel.CENTER_ALIGNMENT
                it.verticalTextPosition = JLabel.BOTTOM
                it.horizontalTextPosition = JLabel.CENTER
                it.font = it.font.deriveFont(it.font.size / 1.25F)
                add(it)
            }
        }

        JLabel(sniper.getTitle()).also {
            it.alignmentX = JPanel.CENTER_ALIGNMENT
            it.verticalTextPosition = JLabel.BOTTOM
            it.horizontalTextPosition = JLabel.CENTER
            add(it)
        }

        val menus = ArrayList<PopupMenu>()
        add(PopupMenuButton("Viewer", "icons/viewer.png", this, { SCViewerWindow(null, config, false) }, menus))
        add(PopupMenuButton("Editor", "icons/editor.png", this, { SCEditorWindow(null, -1, -1, "SnipSniper Editor", config, true, null, inClipboard = false, isStandalone = false) }, menus))
        add(JSeparator())
        add(PopupMenuButton("menu_open_image_folder".translate(), "icons/folder.png", this, {
            config.getString(ConfigHelper.PROFILE.lastSaveFolder).also { lsf ->
                if(lsf.isEmpty() || lsf == "none" || !FileUtils.exists(lsf))
                    FileUtils.openFolder(config.getString(ConfigHelper.PROFILE.pictureFolder))
                else FileUtils.openFolder(lsf)
            }
        }, menus))
        add(PopupMenuButton("menu_config".translate(), "icons/config.png", this, { SnipSniper.openConfigWindow(sniper) }, menus))

        if(SnipSniper.isDebug()) {
            PopupMenu("Debug", "icons/debug.png".getImage()).also { pm ->
                pm.add(PopupMenuButton("Console", "icons/console.png", this, { CCLogger.enableDebugConsole(true) }, menus))
                pm.add(PopupMenuButton("Open log folder", "icons/folder.png", this, { FileUtils.openFolder(SnipSniper.logFolder) }, menus))
                pm.add(PopupMenuButton("Language test", "icons/config.png", this, { LangDebugWindow() }, menus))
                add(pm).also { menus.add(pm) }
            }
        }

        add(PopupMenuButton("menu_about".translate(), "icons/about.png", this, { AboutWindow(sniper) }, menus))
        add(JSeparator())
        add(PopupMenuButton("Restart", "icons/restart.png", this, { SnipSniper.restart() }, menus))
        add(PopupMenuButton("menu_quit".translate(), "icons/redx.png", this, { SnipSniper.exit(false) }, menus))

        addFocusListener(object: FocusAdapter() {
            override fun focusLost(e: FocusEvent) = kotlin.run { isVisible = false }
        })
    }

    fun showPopup(x: Int, y: Int) {
        isVisible = true
        pack()
        setLocation(x - width, y - height)
        isAlwaysOnTop = true
        requestFocus()
    }
}