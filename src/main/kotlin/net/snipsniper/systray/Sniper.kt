package net.snipsniper.systray

import net.snipsniper.ImageManager
import net.snipsniper.SnipSniper
import net.snipsniper.capturewindow.CaptureWindow
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.scaledSmooth
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.mouse.NativeMouseAdapter
import org.jnativehook.mouse.NativeMouseEvent
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class Sniper(val profileID: Int) {
    private var captureWindow: CaptureWindow? = null
    var config: Config = Config("profile${profileID}.cfg", "profile_defaults.cfg")
        private set
    lateinit var trayIcon: TrayIcon
        private set
    private val instance = this

    private var nativeKeyAdapter: NativeKeyAdapter
    private var nativeMouseAdapter: NativeMouseAdapter
    //TODO: Maybe make a function for opening the capture window so that we call isIdle = false at a unified place?
    init {
        CCLogger.log("Loading profile $profileID", CCLogLevel.DEBUG)
        if(SystemTray.isSupported()) {
            val popup = Popup(this)
            val tray = SystemTray.getSystemTray()
            val image = ImageUtils.getIconDynamically(config.getString(ConfigHelper.PROFILE.icon))?.scaledSmooth(16, 16) ?: ImageUtils.getDefaultIcon(profileID)
            image.flush()
            trayIcon = TrayIcon(image, "SnipSniper(${getTitle()})")
            trayIcon.isImageAutoSize = true
            trayIcon.addMouseListener(object: MouseAdapter() {
                fun showPopup(e: MouseEvent) { if(e.isPopupTrigger) popup.showPopup(e.x, e.y) }

                override fun mouseReleased(e: MouseEvent) = showPopup(e)
                override fun mousePressed(e: MouseEvent) = showPopup(e)
                override fun mouseClicked(e: MouseEvent) {
                    if(e.button == 1 && captureWindow == null && SnipSniper.isIdle) {
                        captureWindow = CaptureWindow(instance)
                        SnipSniper.isIdle = false
                    }
                }
            })
            tray.add(trayIcon)
        }

        nativeKeyAdapter = object : NativeKeyAdapter() {
            override fun nativeKeyPressed(e: NativeKeyEvent) {
                checkNativeKey("KB", e.keyCode, e.keyLocation)
            }
        }

        nativeMouseAdapter = object : NativeMouseAdapter() {
            override fun nativeMouseClicked(e: NativeMouseEvent) {
                checkNativeKey("M", e.button, -1)
            }
        }
        GlobalScreen.addNativeKeyListener(nativeKeyAdapter)
        GlobalScreen.addNativeMouseListener(nativeMouseAdapter)
    }

    fun kill() {
        GlobalScreen.removeNativeKeyListener(nativeKeyAdapter)
        GlobalScreen.removeNativeMouseListener(nativeMouseAdapter)
        SystemTray.getSystemTray().remove(trayIcon)
    }

    fun getIconString(): String = config.getString(ConfigHelper.PROFILE.icon)

    fun killCaptureWindow() {
        if(captureWindow != null) {
            if(SystemTray.isSupported() && getIconString() == "none") trayIcon.image = ImageManager.getImage("systray/icon$profileID.png")
            SnipSniper.isIdle = true
            captureWindow = null
            System.gc()
        }
    }

    fun checkNativeKey(identifier: String, pressedKey: Int, pressedLocation: Int) {
        var hotkey = config.getString(ConfigHelper.PROFILE.hotkey)
        if(hotkey != "NONE") {
            if(hotkey.startsWith(identifier)) {
                var location = -1
                if(pressedLocation != -1 && hotkey.contains("_")) {
                    val parts = hotkey.split("_")
                    hotkey = parts[0]
                    location = Integer.parseInt(parts[1])
                }
                val key = Integer.parseInt(hotkey.replace(identifier, ""))
                if(pressedKey == key && (location == -1 || location == pressedLocation)) {
                    if(captureWindow == null && SnipSniper.isIdle) {
                        captureWindow = CaptureWindow(instance)
                        SnipSniper.isIdle = false
                    }
                }
            }
        }
    }

    fun getTitle(): String {
        var title = config.getString(ConfigHelper.PROFILE.title)
        if(title == "none") title = "Profile $profileID"
        return title
    }
}