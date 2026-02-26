package net.snipsniper

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import net.snipsniper.configwindow.ConfigWindow
import net.snipsniper.sceditor.SCEditorWindow
import net.snipsniper.scviewer.SCViewerWindow
import net.snipsniper.systray.Sniper
import net.snipsniper.utils.*
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.defaultdepot.StringUtils
import org.capturecoop.legiblelogger.LegibleLogFilter
import org.capturecoop.legiblelogger.LegibleLogLevel
import org.capturecoop.legiblelogger.LegibleLogger
import java.awt.Desktop
import java.awt.GraphicsEnvironment
import java.awt.SystemTray
import java.awt.Toolkit
import java.awt.Window
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JDialog
import javax.swing.JFrame
import javax.swing.SwingUtilities
import javax.swing.UIManager
import kotlin.system.exitProcess


class SnipSniper {
    companion object {
        val defaultProfileConfig = Config("", "profile_defaults.cfg", true)
        val defaultGlobalConfig = Config("", "main_defaults.cfg", true)

        const val PROFILE_COUNT: Int = 8
        lateinit var config: Config
            private set
        private lateinit var args: Array<String>
        var isDemo = false
        var isIdle = true

        var jarFolder: String = ""
            private set
            get() { return StringUtils.correctSlashes(field) }
        var mainFolder: String = ""
            private set
            get() { return StringUtils.correctSlashes(field) }
        var configFolder: String = ""
            private set
            get() { return StringUtils.correctSlashes(field) }
        var logFolder: String = ""
            private set
            get() { return StringUtils.correctSlashes(field) }
        var imgFolder: String = ""
            private set
            get() { return StringUtils.correctSlashes(field) }

        private var configWindow: ConfigWindow? = null
        private val profiles = arrayOfNulls<Sniper>(PROFILE_COUNT)
        lateinit var buildInfo: BuildInfo
            private set
        lateinit var platformType: PlatformType
            private set

        private lateinit var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler

        fun start(args: Array<String>) {
            this.args = args

            System.setProperty("sun.java2d.uiScale", "1")

            LegibleLogger.enabled = true
            LegibleLogger.paused = true //Allows us setting up things like log file and format before having it log

            NativeHookManager.disableLogger()

            val launchType = Utils.getLaunchType(System.getProperty("launchType"))
            platformType = Utils.getPlatformType(System.getProperty("platformType"))

            Config("buildinfo.cfg", "buildinfo.cfg", true).also {
                buildInfo = BuildInfo(it)
            }

            //This is done here, not further below so that if we run a command like -version we dont save anything to disk!
            val cmdline = CommandLineHelper().also { it.handle(args) }

            //Set folders
            kotlin.run {
                jarFolder = when(platformType) {
                    PlatformType.STEAM, PlatformType.WIN_INSTALLED -> System.getProperty("user.home")
                    else -> FileUtils.getJarFolder()
                }
                mainFolder =    "$jarFolder/SnipSniper"
                configFolder =  "$mainFolder/cfg/"
                logFolder =     "$mainFolder/logs/"
                imgFolder =     "$mainFolder/img/"
            }

            if (!isDemo) {
                if (!FileUtils.mkdirs(configFolder, logFolder, imgFolder)) {
                    LegibleLogger.log("Could not create required folders! Exiting...", LegibleLogLevel.ERROR)
                    exit(false)
                }
            }

            config = Config("main.cfg", "main_defaults.cfg")

            val logFileName = LocalDateTime.now().toString().replace(".", "_").replace(":", "_") + ".log"
            LegibleLogger.logFormat = config.getString(ConfigHelper.MAIN.logFormat)
            LegibleLogger.logFile = File(logFolder, logFileName)
            LegibleLogger.gitHubCodePathURL = "https://github.com/CaptureCoop/SnipSniper/tree/${buildInfo.gitHash}/src/main/java/"
            LegibleLogger.gitHubCodeClassPath = "net.snipsniper"
            LegibleLogger.paused = false

            uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, throwable ->
                LegibleLogger.logStacktrace(throwable, LegibleLogLevel.ERROR)
            }
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

            StatsManager.init()
            StatsManager.incrementCount(StatsManager.STARTED_AMOUNT)

            //TODO: Try not expression
            if (!cmdline.language.isNullOrEmpty())
                config.set(ConfigHelper.MAIN.language, cmdline.language ?: throw Exception("Language is set per argument bus is null!"))

            if (cmdline.isDebug) {
                config.set(ConfigHelper.MAIN.debug, "true")
                LegibleLogger.filter = LegibleLogFilter.DEBUG //Overwrite
            }

            ImageManager.loadResources()

            System.setProperty("sun.java2d.uiScale", "1.0")

            when (config.getString(ConfigHelper.MAIN.theme)) {
                "dark" -> UIManager.setLookAndFeel(FlatDarculaLaf())
                else -> UIManager.setLookAndFeel(FlatIntelliJLaf())
            }
            UIManager.put("ScrollBar.showButtons", true)
            UIManager.put("ScrollBar.width", 16)
            UIManager.put("TabbedPane.showTabSeparators", true)

            JFrame.setDefaultLookAndFeelDecorated(true)
            JDialog.setDefaultLookAndFeelDecorated(true)
            setUIScale()

            LangManager.load()
            WikiManager.load(LangManager.getLanguage())

            LegibleLogger.info("Launching SnipSniper Version ${buildInfo.version}")
            buildInfo.log()
            SystemInfo.log()

            if (!SystemUtils.IS_OS_WINDOWS) {
                LegibleLogger.warn("=================================================================================")
                LegibleLogger.warn("= SnipSniper Linux is still in development and may not work properly or at all. =")
                LegibleLogger.warn("=                        !!!!! USE WITH CAUTION !!!!                            =")
                LegibleLogger.warn("=================================================================================")
            }

            LegibleLogger.debug("========================================")
            LegibleLogger.debug("= SnipSniper is running in debug mode! =")
            LegibleLogger.debug("========================================")

            config.getString(ConfigHelper.MAIN.language).also {
                if (!LangManager.languages.contains(it)) {
                    LegibleLogger.error("Language <$it> not found. Available languages: ${LangManager.languages}")
                    exit(false)
                }
            }

            config.save()

            if (isDemo) {
                LegibleLogger.warn("============================================================")
                LegibleLogger.warn("= SnipSniper is running in DEMO mode                       =")
                LegibleLogger.warn("= This means that no files will be created and/or modified =")
                LegibleLogger.warn("============================================================")
            }

            if (cmdline.editorOnly || launchType == LaunchType.EDITOR) {
                val editorConfig = SCEditorWindow.standaloneEditorConfig
                editorConfig.save()

                var img: BufferedImage? = null
                var path = ""
                if (!cmdline.editorFile.isNullOrEmpty() || (launchType == LaunchType.EDITOR && args.isNotEmpty())) {
                    try {
                        path = cmdline.editorFile!!.ifEmpty { args[0] }
                        File(path).also { if(it.exists()) img = ImageIO.read(it) }
                    } catch (ioException: IOException) {
                        LegibleLogger.error("Error reading image file for editor, path: $path")
                        LegibleLogger.logStacktrace(ioException, LegibleLogLevel.ERROR)
                    }
                }
                SCEditorWindow(img, -1, -1, "SnipSniper Editor", editorConfig, false, path, false, true)
            } else if(cmdline.viewerOnly || launchType == LaunchType.VIEWER) {
                var file: File? = null
                if(cmdline.viewerFile != null && cmdline.viewerFile!!.isNotEmpty())
                    file = File(cmdline.viewerFile!!)

                if(launchType == LaunchType.VIEWER) {
                    if(args.isNotEmpty()) {
                        file = File(args[0])
                        if(!file.exists()) file = null
                    }
                }
                SCViewerWindow(file, null, true)
            } else {
                resetProfiles()
            }
        }

        fun resetProfiles() {
            LegibleLogger.info("Resetting/Starting profiles...")
            if(SystemTray.isSupported()) {
                val tray = SystemTray.getSystemTray()
                tray.trayIcons.forEach { tray.remove(it) }
            }

            profiles.forEach { it?.kill() }
            Arrays.fill(profiles, null)

            profiles[0] = Sniper(0).also { it.config.save() }

            if(!SystemTray.isSupported()) profiles[0]?.let { profile -> openConfigWindow(profile.config, ConfigWindow.PAGE.GeneralPanel) }
            for(i in 1 until PROFILE_COUNT) {
                if(File(configFolder, "profile${i}.cfg").exists())
                    profiles[i] = Sniper(i)
            }
        }

        fun exit(exitForRestart: Boolean) {
            if(config.getBool(ConfigHelper.MAIN.debug)) {
                if (!exitForRestart && Desktop.isDesktopSupported())
                    Desktop.getDesktop().open(LegibleLogger.logFile)
            }
            LegibleLogger.info("Exit requested. Goodbye!")
            NativeHookManager.exit()
            exitProcess(0)
        }

        fun openConfigWindow(config: Config?, page: ConfigWindow.PAGE) {
            if(configWindow == null) {
                configWindow = ConfigWindow(config, page)
                configWindow?.addCustomWindowListener { configWindow = null }
            } else {
                configWindow?.requestFocus()
            }
        }

        fun getProfileCount(): Int {
            var amount = 0
            profiles.forEach { if(it != null) amount++ }
            return amount
        }

        fun refreshGlobalConfigFromDisk() { config = Config("main.cfg", "main_defaults.cfg") }

        fun getProfile(id: Int): Sniper? = if(profiles.size < id) null else profiles[id]

        fun setProfile(id: Int, sniper: Sniper) { profiles[id] = sniper }

        fun refreshTheme() {
            when(config.getString(ConfigHelper.MAIN.theme)) {
                "dark" -> UIManager.setLookAndFeel(FlatDarculaLaf())
                else -> UIManager.setLookAndFeel(FlatIntelliJLaf())
            }
        }

        fun getNewThread(f: () -> (Unit)): Thread = Thread { f.invoke() }.also { thread -> thread.uncaughtExceptionHandler = uncaughtExceptionHandler }

        fun restart() {
            //TODO: Add for other platform types!
            when(platformType) {
                PlatformType.JAR -> Utils.restartApplication(*args)
                else -> LegibleLogger.log("Warning: Restart has not been implemented for this platform! ($platformType)", LegibleLogLevel.WARNING)
            }
        }

        fun openConfigWindow(sniper: Sniper) = openConfigWindow(sniper.config, ConfigWindow.PAGE.GeneralPanel)

        fun openConfigWindow(editor: SCEditorWindow) = openConfigWindow(editor.config, ConfigWindow.PAGE.EditorPanel)

        fun isDebug(): Boolean = config.getBool(ConfigHelper.MAIN.debug)

        fun getVersionString(): String = buildInfo.version.toString()

        fun getSysUIScale(): Float = Toolkit.getDefaultToolkit().screenResolution / 96.toFloat()

        fun calculateEffectiveUIScale(size: Int): Int = (size * getEffectiveUIScale()).toInt()

        fun getEffectiveUIScale(): Float {
            val scaleValue = config.getString(ConfigHelper.MAIN.uiScaling)
            return if(scaleValue == "auto") getSysUIScale() else scaleValue.toFloat()
        }

        fun setUIScale() {
            val scaleValue = config.getString(ConfigHelper.MAIN.uiScaling).let {
                if(it == "auto") getSysUIScale() else it.toFloat()
            }
            LegibleLogger.info("Setting FlatLaf Scale to $scaleValue")
            System.setProperty("flatlaf.uiScale", scaleValue.toString())
            UIManager.put("defaultFont", FontLoader.defaultFont.deriveFont(FontLoader.defaultFontSize * scaleValue))
            UIManager.setLookAndFeel(UIManager.getLookAndFeel())

            for (w in Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(w)
                w.pack()
                w.invalidate()
                w.validate()
                w.repaint()
            }
        }
    }
}