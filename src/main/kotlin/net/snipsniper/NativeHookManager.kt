package net.snipsniper

import org.capturecoop.cclogger.CCLogger
import org.jnativehook.GlobalScreen
import org.jnativehook.NativeHookException

object NativeHookManager {

    private fun verifyRegistered() {
        if(GlobalScreen.isNativeHookRegistered()) return

        CCLogger.info("Registering NativeHook")
        try {
            GlobalScreen.registerNativeHook()
        } catch (ex: NativeHookException) {
            CCLogger.error("Could not register NativeHooK! Message: ${ex.message}")
        }
    }

    fun exit() {
        CCLogger.info("Unregistering NativeHook")
        GlobalScreen.unregisterNativeHook()
    }
}