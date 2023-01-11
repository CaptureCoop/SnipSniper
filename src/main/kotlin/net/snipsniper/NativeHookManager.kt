package net.snipsniper

import org.capturecoop.cclogger.CCLogger
import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException
import org.jnativehook.keyboard.NativeKeyAdapter
import org.jnativehook.keyboard.NativeKeyEvent
import org.jnativehook.keyboard.NativeKeyListener
import org.jnativehook.mouse.NativeMouseAdapter
import org.jnativehook.mouse.NativeMouseEvent
import org.jnativehook.mouse.NativeMouseListener
import java.util.logging.Level
import java.util.logging.LogManager
import java.util.logging.Logger

data class NativeHookEvent(val code: Int, val type: Type, val location: Int) {
    enum class Type(val code: String) { MOUSE("M"), KEYBOARD("KB")}
}

class NativeHookInstance(private val clazz: Any) {
    private val keyListener: NativeKeyListener
    private val mouseListener: NativeMouseListener
    private val listeners = ArrayList<(NativeHookEvent) -> Unit>()

    init {
        keyListener = object: NativeKeyAdapter() {
            override fun nativeKeyPressed(event: NativeKeyEvent) {
                listeners.forEach {
                    it.invoke(NativeHookEvent(event.keyCode, NativeHookEvent.Type.KEYBOARD, event.keyLocation))
                }
            }
        }
        mouseListener = object: NativeMouseAdapter() {
            override fun nativeMouseClicked(event: NativeMouseEvent) {
                listeners.forEach {
                    it.invoke(NativeHookEvent(event.button, NativeHookEvent.Type.MOUSE, -1))
                }
            }
        }
        GlobalScreen.addNativeKeyListener(keyListener)
        GlobalScreen.addNativeMouseListener(mouseListener)
        CCLogger.info("$clazz is now listening")
    }

    fun addListener(action: (NativeHookEvent) -> (Unit)) = listeners.add(action)

    fun unregister() {
        GlobalScreen.removeNativeKeyListener(keyListener)
        GlobalScreen.removeNativeMouseListener(mouseListener)
        CCLogger.info("$clazz is no longer listening")
    }
}

object NativeHookManager {
    const val VC_ESCAPE = NativeKeyEvent.VC_ESCAPE
    private val profiles = HashMap<Any, NativeHookInstance>()

    private fun verifyRegistered() {
        if(GlobalScreen.isNativeHookRegistered()) return

        CCLogger.info("Registering NativeHook")
        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            CCLogger.error("Could not register NativeHooK! Message: ${ex.message}")
        }
    }

    fun register(clazz: Any): NativeHookInstance {
        CCLogger.info("$clazz is trying to register a NativeHookInstance")
        verifyRegistered()
        return NativeHookInstance(clazz).also { profiles[clazz] = it }
    }

    fun unregister(clazz: Any) {
        profiles.remove(clazz)?.unregister()
    }

    fun exit() {
        CCLogger.info("Unregistering NativeHook")
        profiles.forEach { (_, instance) ->  instance.unregister() }
        GlobalScreen.unregisterNativeHook()
    }

    fun disableLogger() {
        LogManager.getLogManager().reset()
        Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF
    }

    fun getKeyText(key: Int): String = NativeKeyEvent.getKeyText(key)
}