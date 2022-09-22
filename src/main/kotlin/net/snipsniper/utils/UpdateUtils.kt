package net.snipsniper.utils

import net.snipsniper.SnipSniper
import net.snipsniper.config.ConfigHelper
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger

class UpdateUtils {
    companion object {
        fun update() {
            val type = SnipSniper.platformType
            if(type == PlatformType.UNKNOWN) return

            val pathInJar = "net/snipsniper/resources/SnipUpdater.jar"
            val updaterLocation = System.getProperty("java.io.tmpdir") + "//SnipUpdater.jar" //TODO: Update this to use createTempFile("name", ".jar")

            when(type) {
                PlatformType.JAR -> {
                    FileUtils.copyFromJar(pathInJar, updaterLocation)
                    Utils.getReleaseType(SnipSniper.config.getString(ConfigHelper.MAIN.updateChannel)).also { channel ->
                        SnipSniper.jarFolder?.let {
                            val jarLink = if(channel == ReleaseType.DEV) Links.DEV_JAR else Links.STABLE_JAR
                            Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", jarLink, "-gui", "-exec", "SnipSniper.jar", "-dir", it)
                            SnipSniper.exit(false)
                        }
                    }
                }
                PlatformType.WIN -> {
                    FileUtils.copyFromJar(pathInJar, updaterLocation)
                    Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_PORTABLE, "-gui", "-extract", "-exec", "SnipSniper.exe", "-dir", FileUtils.getCanonicalPath("."), "-deleteFile")
                    SnipSniper.exit(false)
                }
                PlatformType.WIN_INSTALLED -> {
                    FileUtils.copyFromJar(pathInJar, updaterLocation)
                    Utils.executeProcess(false, "java", "-jar", updaterLocation, "-url", Links.STABLE_INSTALLER, "-gui", "-exec", "SnipSniper_Installer_Win.exe", "-dir", System.getProperty("java.io.tmpdir"))
                    SnipSniper.exit(false)
                }
                else -> CCLogger.log("Updating for platform $type not currently supported!", CCLogLevel.WARNING)
            }
        }
    }
}