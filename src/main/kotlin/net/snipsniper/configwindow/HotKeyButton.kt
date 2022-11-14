package net.snipsniper.configwindow

import net.snipsniper.utils.translate
import org.jnativehook.GlobalScreen
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.jnativehook.mouse.NativeMouseEvent
import org.jnativehook.mouse.NativeMouseListener
import javax.swing.JButton
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class HotKeyButton(key: String): JButton(), NativeKeyListener, NativeMouseListener {
    private var listening = false
    var hotkey = 0
    //private val instance: HotKeyButton? = null
    private var isKeyboard = true
    private var location = -1
    private var oldLabel: String? = null
    private var listeners = ArrayList<ChangeListener>()

    init {
        var tempKey = key
        if(tempKey.contains("_")) {
            tempKey.split("_").also {
                tempKey = it[0]
                location = it[1].toInt()
            }
        }

        if(tempKey.startsWith("NONE")) {
            text = "config_label_none".translate()
            hotkey = -1
        } else if(tempKey.startsWith("KB")) {
            hotkey = tempKey.replace("KB", "").toInt()
            text = NativeKeyEvent.getKeyText(hotkey)
        } else if (tempKey.startsWith("M")) {
            hotkey = tempKey.replace("M", "").toInt()
            isKeyboard = false
            text = "${"config_label_mouse".translate()} $hotkey"
        }

        GlobalScreen.addNativeKeyListener(this)
        GlobalScreen.addNativeMouseListener(this)
        addActionListener {
            listening = true
            text = "config_label_hotkey_listening".translate()
        }

        oldLabel = text
    }

    override fun nativeKeyTyped(nativeKeyEvent: NativeKeyEvent) { }

    override fun nativeKeyPressed(nativeKeyEvent: NativeKeyEvent) {
        if(listening) {
            if(nativeKeyEvent.keyCode == NativeKeyEvent.VC_ESCAPE) {
                listening = false
                text = oldLabel
            } else {
                isKeyboard = true
                hotkey = nativeKeyEvent.keyCode
                location = nativeKeyEvent.keyLocation
                listening = false
                text = NativeKeyEvent.getKeyText(hotkey)
                oldLabel = text
                notifyListeners()
            }
        }
    }

    override fun nativeKeyReleased(nativeKeyEvent: NativeKeyEvent) { }

    override fun nativeMouseClicked(nativeMouseEvent: NativeMouseEvent) { }

    override fun nativeMousePressed(nativeMouseEvent: NativeMouseEvent) {
        if(listening) {
            hotkey = nativeMouseEvent.button
            if(hotkey == 1 || hotkey == 2) {
                hotkey = -1
                return
            }
            location = -1
            isKeyboard = false
            listening = false
            notifyListeners()
            text = "${"config_label_mouse".translate()} $hotkey"
            oldLabel = text
        }
    }

    private fun notifyListeners() = listeners.forEach { it.stateChanged(ChangeEvent(this)) }

    override fun nativeMouseReleased(nativeMouseEvent: NativeMouseEvent) { }

    fun getHotKeyString(): String {
        val hotkeyModifier = if(isKeyboard) "KB" else "M"
        val locationModifier = if(location != -1) "_$location" else ""
        return hotkeyModifier + hotkey + locationModifier
    }
}