package net.snipsniper.systray

import net.snipsniper.ImageManager
import net.snipsniper.NativeHookManager
import net.snipsniper.SnipSniper
import net.snipsniper.capturewindow.CaptureWindow
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.*
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import java.awt.Image
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class Sniper(private val profileID: Int) {
    private var captureWindow: CaptureWindow? = null
    var config: Config = Config("profile${profileID}.cfg", "profile_defaults.cfg")
        private set
    private lateinit var trayIcon: TrayIcon
    private val instance = this

    private var isCustomIcon = false
    //TODO: Maybe make a function for opening the capture window so that we call isIdle = false at a unified place?
    //TODO: We check twice if iconString or whatever is none, we should not statically check that but maybe check with the defaults?
    init {
        CCLogger.log("Loading profile $profileID", CCLogLevel.DEBUG)
        if(SystemTray.isSupported()) {
            val popup = Popup(this)
            val tray = SystemTray.getSystemTray()
            val image = ImageUtils.getIconDynamically(config.getString(ConfigHelper.PROFILE.icon))?.scaledSmooth(16, 16).also { isCustomIcon = true } ?: getTrayIcon(profileID)
            image.flush()
            trayIcon = TrayIcon(image, "SnipSniper(${getTitle()})")
            trayIcon.isImageAutoSize = true
            trayIcon.addMouseListener(object: MouseAdapter() {
                fun showPopup(e: MouseEvent) { if(e.isPopupTrigger) popup.showPopup(e.x, e.y) }

                override fun mouseReleased(e: MouseEvent) = showPopup(e)
                override fun mousePressed(e: MouseEvent) = showPopup(e)
                override fun mouseClicked(e: MouseEvent) { if(e.button == 1) openCaptureWindow() }
            })
            tray.add(trayIcon)
        }
        //Register and listen
        if(config.getString(ConfigHelper.PROFILE.hotkey) != "NONE") {
            NativeHookManager.register(this).addListener {
                checkNativeKey(it.type.code, it.code, it.location)
            }
        }
    }

    fun kill() {
        NativeHookManager.unregister(this)
        SystemTray.getSystemTray().remove(trayIcon)
    }

    private fun checkNativeKey(identifier: String, pressedKey: Int, pressedLocation: Int) {
        var hotkey = config.getString(ConfigHelper.PROFILE.hotkey)
        if(hotkey == "NONE") return
        if(hotkey.startsWith(identifier)) {
            var location = -1
            if(pressedLocation != -1 && hotkey.contains("_")) {
                val parts = hotkey.split("_")
                hotkey = parts[0]
                location = Integer.parseInt(parts[1])
            }
            if(
                pressedKey == hotkey.replace(identifier, "").toInt() &&
                (location == -1 || location == pressedLocation)
            ) openCaptureWindow()
        }
    }

    fun getTitle(): String {
        config.getString(ConfigHelper.PROFILE.title).also {
            return if(it == config.getDefault(ConfigHelper.PROFILE.icon)) "Profile $profileID" else it
        }
    }

    private fun openCaptureWindow() {
        if(captureWindow == null && SnipSniper.isIdle) {
            if(SystemTray.isSupported() && isCustomIcon) trayIcon.image = getTrayIcon(profileID, true)
            captureWindow = CaptureWindow(instance)
            SnipSniper.isIdle = false
        } else {
            captureWindow?.requestFocus()
        }
    }

    fun killCaptureWindow() {
        if(captureWindow != null) {
            if(SystemTray.isSupported() && isCustomIcon) trayIcon.image = getTrayIcon(profileID)
            SnipSniper.isIdle = true
            captureWindow = null
            System.gc()
        }
    }

    //Note: This does not handle custom images
    private fun getTrayIcon(profileID: Int, alt: Boolean = false) = if(alt) "systray/alt_icon$profileID.png".getImage() else "systray/icon$profileID.png".getImage()

    fun alert(message: String, title: String, type: TrayIcon.MessageType) = trayIcon.displayMessage(message, title, type)

    override fun toString() = "SnipSniper Profile ($profileID)"
}