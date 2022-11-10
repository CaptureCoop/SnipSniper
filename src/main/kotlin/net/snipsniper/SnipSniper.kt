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
import org.capturecoop.cclogger.CCLogFilter
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCStringUtils
import org.jnativehook.GlobalScreen
import java.awt.Desktop
import java.awt.SystemTray
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.net.URLDecoder
import java.nio.file.Paths
import java.time.LocalDateTime
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.JDialog
import javax.swing.JFrame
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

        var jarFolder: String? = null
            private set
            get() {return CCStringUtils.correctSlashes(field)}
        var mainFolder: String? = null
            private set
            get() {return CCStringUtils.correctSlashes(field)}
        var configFolder: String? = null
            private set
            get() {return CCStringUtils.correctSlashes(field)}
        var logFolder: String? = null
            private set
            get() {return CCStringUtils.correctSlashes(field)}
        var imgFolder: String? = null
            private set
            get() {return CCStringUtils.correctSlashes(field)}

        private var configWindow: ConfigWindow? = null
        private val profiles = arrayOfNulls<Sniper>(PROFILE_COUNT)
        lateinit var buildInfo: BuildInfo
            private set
        lateinit var platformType: PlatformType
            private set

        private lateinit var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler

        fun start(args: Array<String>) {
            this.args = args

            System.setProperty("sun.java2d.uiScale.enabled", "false")
            System.setProperty("sun.java2d.uiScale", "1")

            if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_LINUX) {
                println("SnipSniper is currently only supported fully on Windows! Proceed at your own caution!")
                exitProcess(0)
            }

            CCLogger.enabled = true
            CCLogger.paused = true //Allows us setting up things like log file and format before having it log

            Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF

            val launchType = Utils.getLaunchType(System.getProperty("launchType"))
            platformType = Utils.getPlatformType(System.getProperty("platformType"))

            Config("buildinfo.cfg", "buildinfo.cfg", true).also {
                buildInfo = BuildInfo(it)
            }

            //This is done here, not further below so that if we run a command like -version we dont save anything to disk!
            val cmdline = CommandLineHelper().also { it.handle(args) }

            when(platformType) {
                PlatformType.STEAM, PlatformType.WIN_INSTALLED -> setSaveLocationToDocuments()
                else -> setSaveLocationToJar()
            }

            if (!isDemo) {
                if (!FileUtils.mkdirs(configFolder, logFolder, imgFolder)) {
                    CCLogger.log("Could not create required folders! Exiting...", CCLogLevel.ERROR)
                    exit(false)
                }
            }

            config = Config("main.cfg", "main_defaults.cfg")

            val logFileName = LocalDateTime.now().toString().replace(".", "_").replace(":", "_") + ".log"
            CCLogger.logFormat = config.getString(ConfigHelper.MAIN.logFormat)
            CCLogger.logFile = File(logFolder, logFileName)
            CCLogger.gitHubCodePathURL = "https://github.com/CaptureCoop/SnipSniper/tree/${buildInfo.gitHash}/src/main/java/"
            CCLogger.gitHubCodeClassPath = "net.snipsniper"
            CCLogger.paused = false

            uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, throwable ->
                CCLogger.error("SnipSniper encountered an uncaught exception. This may be fatal!")
                CCLogger.logStacktrace(throwable, CCLogLevel.ERROR)
            }
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

            GlobalScreen.registerNativeHook()

            StatsManager.init()
            StatsManager.incrementCount(StatsManager.STARTED_AMOUNT)

            //TODO: Try not expression
            if (!cmdline.language.isNullOrEmpty())
                config.set(ConfigHelper.MAIN.language, cmdline.language)

            if (cmdline.isDebug) {
                config.set(ConfigHelper.MAIN.debug, "true")
                CCLogger.filter = CCLogFilter.DEBUG //Overwrite
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

            LangManager.load()
            WikiManager.load(LangManager.getLanguage())

            CCLogger.info("Launching SnipSniper Version ${buildInfo.version.digitsToString()} (rev-${buildInfo.gitHash})")
            buildInfo.run {
                CCLogger.info("")
                CCLogger.info("== Build Info ==")
                CCLogger.info("Type: $releaseType")
                CCLogger.info("Version: ${getVersionString()}")
                CCLogger.info("Build date: $buildDate")
                CCLogger.info("GitHash: $gitHash")
                CCLogger.info("GitHash Full: $gitHashFull")
                CCLogger.info("Branch: $branch")
                CCLogger.info("OS Name: $osName")
                CCLogger.info("OS Version: $osVersion")
                CCLogger.info("OS Arch: $osArch")
                CCLogger.info("Java Vendor: $javaVendor")
                CCLogger.info("Java Version: $javaVersion")
                CCLogger.info("")
            }

            SystemInfo.run {
                CCLogger.info("== System Info ==")
                CCLogger.info("OS Name: ${getName()}")
                CCLogger.info("OS Version: ${getVersion()}")
                CCLogger.info("OS Arch: ${getArch()}")
                CCLogger.info("OS Date/Time: ${getTimeAndDate()} (${getTimeZone()})")
                CCLogger.info("OS Memory: Free(${getFreePhysicalMemory().prettyPrintBytes()}), Total(${getPhysicalMemory().prettyPrintBytes()})")
                CCLogger.info("Java Vendor: ${getJavaVendor()}")
                CCLogger.info("Java Version: ${getJavaVersion()}")
                CCLogger.info("Java Memory: Free(${getFreeJavaMemory().prettyPrintBytes()}), Total Allocated(${getTotalJavaMemory().prettyPrintBytes()}), Max(${getMaxJavaMemory().prettyPrintBytes()})")
                CCLogger.info("")
            }

            if (SystemUtils.IS_OS_LINUX) {
                CCLogger.warn("=================================================================================")
                CCLogger.warn("= SnipSniper Linux is still in development and may not work properly or at all. =")
                CCLogger.warn("=                        !!!!! USE WITH CAUTION !!!!                            =")
                CCLogger.warn("=================================================================================")
            }

            CCLogger.debug("========================================")
            CCLogger.debug("= SnipSniper is running in debug mode! =")
            CCLogger.debug("========================================")

            config.getString(ConfigHelper.MAIN.language).also {
                if (!LangManager.languages.contains(it)) {
                    CCLogger.error("Language <$it> not found. Available languages: ${LangManager.languages}")
                    exit(false)
                }
            }

            config.save()

            if (isDemo) {
                CCLogger.warn("============================================================")
                CCLogger.warn("= SnipSniper is running in DEMO mode                       =")
                CCLogger.warn("= This means that no files will be created and/or modified =")
                CCLogger.warn("============================================================")
            }

            if (cmdline.isEditorOnly || launchType == LaunchType.EDITOR) {
                val editorConfig = SCEditorWindow.getStandaloneEditorConfig()
                editorConfig.save()

                var img: BufferedImage? = null
                var path = ""
                if (!cmdline.editorFile.isNullOrEmpty() || (launchType == LaunchType.EDITOR && args.isNotEmpty())) {
                    try {
                        path = cmdline.editorFile.ifEmpty { args[0] }
                        File(path).also { if(it.exists()) img = ImageIO.read(it) }
                    } catch (ioException: IOException) {
                        CCLogger.error("Error reading image file for editor, path: $path")
                        CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
                    }
                }
                SCEditorWindow(img, -1, -1, "SnipSniper Editor", editorConfig, false, path, false, true)
            } else if(cmdline.isViewerOnly || launchType == LaunchType.VIEWER) {
                var file: File? = null
                if(cmdline.viewerFile != null && cmdline.viewerFile.isNotEmpty())
                    file = File(cmdline.viewerFile)

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
            CCLogger.info("Resetting/Starting profiles...")
            if(SystemTray.isSupported()) {
                val tray = SystemTray.getSystemTray()
                tray.trayIcons.forEach { tray.remove(it) }
            }

            profiles.forEach { it?.kill() }
            Arrays.fill(profiles, null)

            profiles[0] = Sniper(0).also { it.config.save() }

            if(!SystemTray.isSupported()) profiles[0]?.let { profile -> openConfigWindow(profile.config, ConfigWindow.PAGE.generalPanel) }
            for(i in 1 until PROFILE_COUNT) {
                if(File(configFolder, "profile${i}.cfg").exists())
                    profiles[i] = Sniper(i)
            }
        }

        private fun setSaveLocationToDocuments() {
            jarFolder = System.getProperty("user.home")
            mainFolder =    "$jarFolder/.SnipSniper"
            configFolder =  "$mainFolder/cfg/"
            logFolder =     "$mainFolder/logs/"
            imgFolder =     "$mainFolder/img/"
        }

        private fun setSaveLocationToJar() {
            //TODO: Test other ways of finding out the jar location
            var folderToUseString: String? = null
            try {
                folderToUseString = URLDecoder.decode(Paths.get(SnipSniper::class.java.protectionDomain.codeSource.location.toURI()).toString(), "UTF-8")
            } catch (exception: Exception) {
                CCLogger.log("Could not set profiles folder. Error: ${exception.message}", CCLogLevel.ERROR)
                exit(false)
            }

            if(folderToUseString != null) {
                val folderToUse = File(folderToUseString)
                jarFolder = if (folderToUse.name.endsWith(".jar"))
                    folderToUseString.replace(folderToUse.name, "")
                else folderToUseString

                mainFolder =    "$jarFolder/SnipSniper"
                configFolder =  "$mainFolder/cfg/"
                logFolder =     "$mainFolder/logs/"
                imgFolder =     "$mainFolder/img/"
            }
        }

        fun exit(exitForRestart: Boolean) {
            if(config.getBool(ConfigHelper.MAIN.debug)) {
                if (!exitForRestart && Desktop.isDesktopSupported())
                    Desktop.getDesktop().open(CCLogger.logFile)
            }
            CCLogger.info("Exit requested. Goodbye!")
            GlobalScreen.unregisterNativeHook()
            exitProcess(0)
        }

        fun openConfigWindow(config: Config, page: ConfigWindow.PAGE) {
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

        fun getNewThread(f: IFunction): Thread = Thread { f.run() }.also { thread -> thread.uncaughtExceptionHandler = uncaughtExceptionHandler }

        fun restart() {
            //TODO: Add for other platform types!
            when(platformType) {
                PlatformType.JAR -> Utils.restartApplication(*args)
                else -> CCLogger.log("Warning: Restart has not been implemented for this platform! ($platformType)", CCLogLevel.WARNING)
            }
        }

        fun openConfigWindow(sniper: Sniper) = openConfigWindow(sniper.config, ConfigWindow.PAGE.generalPanel)

        fun openConfigWindow(editor: SCEditorWindow) = openConfigWindow(editor.config, ConfigWindow.PAGE.editorPanel)

        fun isDebug(): Boolean = config.getBool(ConfigHelper.MAIN.debug)

        fun getVersionString(): String = "${buildInfo.version.digitsToString()}-${buildInfo.releaseType} rev-${buildInfo.gitHash}"
    }
}