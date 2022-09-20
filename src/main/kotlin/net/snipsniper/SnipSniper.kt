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
import java.time.format.DateTimeFormatter
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
        const val PROFILE_COUNT: Int = 8
        lateinit var version: Version
            private set
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

        private lateinit var uncaughtExceptionHandler: Thread.UncaughtExceptionHandler

        fun start(args: Array<String>) {
            this.args = args

            if (!SystemUtils.IS_OS_WINDOWS && !SystemUtils.IS_OS_LINUX) {
                println("SnipSniper is currently only supported fully on Windows! Proceed at your own caution!")
                exitProcess(0)
            }

            CCLogger.setEnabled(true)
            CCLogger.setPaused(true) //Allows us setting up things like log file and format before having it log

            Logger.getLogger(GlobalScreen::class.java.`package`.name).level = Level.OFF

            val launchType = Utils.getLaunchType(System.getProperty("launchType"))

            val buildInfo = Config("buildinfo.cfg", "buildinfo.cfg", true)
            val releaseType = Utils.getReleaseType(buildInfo.getString(ConfigHelper.BUILDINFO.type))
            val platformType = Utils.getPlatformType(System.getProperty("platform"))
            val digits = buildInfo.getString(ConfigHelper.BUILDINFO.version)
            val buildDate = buildInfo.getString(ConfigHelper.BUILDINFO.builddate)
            val githash = buildInfo.getString(ConfigHelper.BUILDINFO.githash)

            version = Version(digits, releaseType, platformType, buildDate, githash)

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
            CCLogger.setLogFormat(config.getString(ConfigHelper.MAIN.logFormat))
            CCLogger.setLogFile(File(logFolder, logFileName))
            CCLogger.setGitHubCodePathURL("https://github.com/CaptureCoop/SnipSniper/tree/${version.githash}/src/main/java/")
            CCLogger.setGitHubCodeClassPath("net.snipsniper")
            CCLogger.setPaused(false)

            uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { _, throwable ->
                CCLogger.log("SnipSniper encountered an uncaught exception. This may be fatal!", CCLogLevel.ERROR)
                CCLogger.logStacktrace(throwable, CCLogLevel.ERROR)
            }
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

            GlobalScreen.registerNativeHook()

            StatsManager.init()
            StatsManager.incrementCount(StatsManager.STARTED_AMOUNT)

            //TODO: Try not expression
            if (!cmdline.language.isNullOrEmpty())
                config.set(ConfigHelper.MAIN.language, cmdline.language)

            if (cmdline.isDebug)
                config.set(ConfigHelper.MAIN.debug, "true")

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

            CCLogger.log("Launching SnipSniper Version ${version.digitsToString()} (rev-${version.githash})")
            CCLogger.log("")
            CCLogger.log("== Build Info ==")
            CCLogger.log("Type: ${buildInfo.getString(ConfigHelper.BUILDINFO.type)}")
            CCLogger.log("Version: ${buildInfo.getString(ConfigHelper.BUILDINFO.version)}")
            CCLogger.log("Build date: ${buildInfo.getString(ConfigHelper.BUILDINFO.builddate)}")
            CCLogger.log("GitHash: ${buildInfo.getString(ConfigHelper.BUILDINFO.githash)}")
            CCLogger.log("GitHash Full: ${buildInfo.getString(ConfigHelper.BUILDINFO.githashfull)}")
            CCLogger.log("Branch: ${buildInfo.getString(ConfigHelper.BUILDINFO.branch)}")
            CCLogger.log("OS Name: ${buildInfo.getString(ConfigHelper.BUILDINFO.osname)}")
            CCLogger.log("OS Version: ${buildInfo.getString(ConfigHelper.BUILDINFO.osversion)}")
            CCLogger.log("OS Arch: ${buildInfo.getString(ConfigHelper.BUILDINFO.osarch)}")
            CCLogger.log("Java Vendor: ${buildInfo.getString(ConfigHelper.BUILDINFO.javavendor)}")
            CCLogger.log("Java Version: ${buildInfo.getString(ConfigHelper.BUILDINFO.javaver)}")
            CCLogger.log("")

            CCLogger.log("== System Info ==")
            CCLogger.log("OS Name: ${System.getProperty("os.name")}")
            CCLogger.log("OS Version: ${Utils.getSystemVersion()}")
            CCLogger.log("OS Arch: ${System.getProperty("os.arch")}")
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")).also {
                CCLogger.log("OS Date/Time: $it (${TimeZone.getDefault().id})")
            }
            CCLogger.log("Java Vendor: ${System.getProperty("java.vendor")}")
            CCLogger.log("Java Version: ${System.getProperty("java.version")}")
            CCLogger.log("Java Version: ${System.getProperty("java.version")}")
            CCLogger.log("")
            if (SystemUtils.IS_OS_LINUX) {
                CCLogger.log("=================================================================================", CCLogLevel.WARNING)
                CCLogger.log("= SnipSniper Linux is still in development and may not work properly or at all. =", CCLogLevel.WARNING)
                CCLogger.log("=                        !!!!! USE WITH CAUTION !!!!                            =", CCLogLevel.WARNING)
                CCLogger.log("=================================================================================", CCLogLevel.WARNING)
            }

            CCLogger.log("========================================", CCLogLevel.DEBUG)
            CCLogger.log("= SnipSniper is running in debug mode! =", CCLogLevel.DEBUG)
            CCLogger.log("========================================", CCLogLevel.DEBUG)

            config.getString(ConfigHelper.MAIN.language).also {
                if (!LangManager.languages.contains(it)) {
                    CCLogger.log("Language <$it> not found. Available languages: ${LangManager.languages}", CCLogLevel.ERROR)
                    exit(false)
                }
            }

            config.save()

            if (isDemo) {
                CCLogger.log("============================================================")
                CCLogger.log("= SnipSniper is running in DEMO mode                       =")
                CCLogger.log("= This means that no files will be created and/or modified =")
                CCLogger.log("============================================================")
            }

            if (cmdline.isEditorOnly || launchType == LaunchType.EDITOR) {
                val editorConfig = SCEditorWindow.getStandaloneEditorConfig()
                editorConfig.save()

                val fileExists: Boolean
                var img: BufferedImage? = null
                var path = ""
                if ((cmdline.editorFile != null && cmdline.editorFile.isNotEmpty()) || (launchType == LaunchType.EDITOR && args.isNotEmpty())) {
                    try {
                        path = if (cmdline.editorFile != null && cmdline.editorFile.isNotEmpty()) cmdline.editorFile
                        else args[0]

                        val file = File(path)
                        fileExists = file.exists()
                        if (fileExists) img = ImageIO.read(file)
                    } catch (ioException: IOException) {
                        CCLogger.log("Error reading image file for editor, path: %c", CCLogLevel.ERROR, path)
                        CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
                    }
                }
                SCEditorWindow(img, -1, -1, "SnipSniper Editor", config, false, path, false, true)
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
            CCLogger.log("Resetting/Starting profiles...")
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
                    Desktop.getDesktop().open(CCLogger.getLogFile())
            }
            CCLogger.log("Exit requested. Goodbye!")
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
            when(version.platformType) {
                PlatformType.JAR -> Utils.restartApplication(*args)
                else -> CCLogger.log("Warning: Restart has not been implemented for this platform! (${version.platformType})", CCLogLevel.WARNING)
            }
        }

        fun openConfigWindow(sniper: Sniper) = openConfigWindow(sniper.config, ConfigWindow.PAGE.generalPanel)

        fun openConfigWindow(editor: SCEditorWindow) = openConfigWindow(editor.config, ConfigWindow.PAGE.editorPanel)

        fun isDebug(): Boolean = config.getBool(ConfigHelper.MAIN.debug)
    }
}