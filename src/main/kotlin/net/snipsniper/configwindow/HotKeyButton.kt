package net.snipsniper.configwindow

import net.snipsniper.NativeHookEvent
import net.snipsniper.NativeHookManager
import net.snipsniper.utils.translate
import javax.swing.JButton
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


class HotKeyButton(key: String): JButton() {
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
            text = NativeHookManager.getKeyText(hotkey)
        } else if (tempKey.startsWith("M")) {
            hotkey = tempKey.replace("M", "").toInt()
            isKeyboard = false
            text = "${"config_label_mouse".translate()} $hotkey"
        }

        addActionListener {
            listening = true
            NativeHookManager.register(this).addListener {
                when(it.type) {
                    NativeHookEvent.Type.KEYBOARD -> key(it)
                    NativeHookEvent.Type.MOUSE -> mouse(it)
                }
                NativeHookManager.unregister(this)
            }
            text = "config_label_hotkey_listening".translate()
        }

        oldLabel = text
    }

    private fun key(event: NativeHookEvent) {
        if(event.code == NativeHookManager.VC_ESCAPE) {
            text = oldLabel
        } else {
            isKeyboard = true
            hotkey = event.code
            location = event.location
            listening = false
            text = NativeHookManager.getKeyText(hotkey)
            oldLabel = text
            notifyListeners()
        }
    }

    private fun mouse(event: NativeHookEvent) {
        hotkey = event.code
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

    private fun notifyListeners() = listeners.forEach { it.stateChanged(ChangeEvent(this)) }

    fun getHotKeyString(): String {
        val hotkeyModifier = if(isKeyboard) "KB" else "M"
        val locationModifier = if(location != -1) "_$location" else ""
        return hotkeyModifier + hotkey + locationModifier
    }

    override fun toString() = "HotKeyButton"
}