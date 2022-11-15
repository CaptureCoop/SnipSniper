package net.snipsniper.utils

import net.snipsniper.SnipSniper
import net.snipsniper.utils.debug.DebugUtils
import kotlin.system.exitProcess

class CommandLineHelper {
    var language: String? = null
    var isRestartedInstance = false
    var isDebug = false
    var editorOnly = false
    var editorFile: String? = null
    var viewerOnly = false
    var viewerFile: String? = null
    private var ignoreNextArg = false

    fun handle(args: Array<String>) {
        if(args.isEmpty()) return
        var doExit = false
        args.forEachIndexed { index, arg ->
            if(ignoreNextArg) {
                ignoreNextArg = false
                return@forEachIndexed
            }
            when(arg) {
                "-help", "-?" -> helpText().also { doExit = true }
                "-version", "-v" -> println(SnipSniper.getVersionString()).also { doExit = true }
                "-demo" -> SnipSniper.isDemo = true //TODO: Put this in here and make SnipSniper pull it
                "-language", "-lang", "-l" -> {
                    if(args.size > index + 1) {
                        language = args[index + 1]
                        ignoreNextArg = true
                    } else println("Missing argument after $arg!")
                }
                "-r" -> isRestartedInstance = true
                "-d", "-debug" -> isDebug = true
                "-editor" -> {
                    editorOnly = true
                    if(args.size > index + 1) {
                        editorFile = args[index + 1]
                        ignoreNextArg = true
                    }
                }
                "-viewer" -> {
                    viewerOnly = true
                    if(args.size > index + 1) {
                        viewerFile = args[index + 1]
                        ignoreNextArg = true
                    }
                }
                "-debugLang" -> DebugUtils.jsonLang().also { doExit = true }
                else -> println("Unrecognized argument <$arg>. Use argument -help to see all the commands!")
            }
        }
        if(doExit) exitProcess(0)
    }

    private fun helpText() {
        println("SnipSniper ${SnipSniper.getVersionString()}\n")
        println("General commands:")
        println("-help / -?     = Displays this")
        println("-version / -v  = Displays version")
        println("-demo          = Starts SnipSniper in demo mode (No configs are being created)")
        println("-language / -l = Sets the language. Useful for demo mode")
        println("-editor        = Starts the standalone editor (You can enter a path after -editor)")
        println("-viewer        = Starts the standalone viewer (You can enter a path after -viewer)")
        println("\nDebug Commands:")
        println("-debug         = Starts SnipSniper in Debug mode")
        println("-debugLang     = Test language files for missing strings")
    }
}