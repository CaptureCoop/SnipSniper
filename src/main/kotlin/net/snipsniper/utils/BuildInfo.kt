package net.snipsniper.utils

import net.snipsniper.SnipSniper
import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper
import org.capturecoop.cclogger.CCLogger

class BuildInfo(config: Config) {
    val version = Version(config.getString(ConfigHelper.BUILDINFO.version))
    val releaseType = Utils.getReleaseType(config.getString(ConfigHelper.BUILDINFO.type))
    val buildDate = config.getString(ConfigHelper.BUILDINFO.builddate)
    val gitHash = config.getString(ConfigHelper.BUILDINFO.githash)
    val gitHashFull = config.getString(ConfigHelper.BUILDINFO.githashfull)
    val branch = config.getString(ConfigHelper.BUILDINFO.branch)
    val osName = config.getString(ConfigHelper.BUILDINFO.osname)
    val osVersion = config.getString(ConfigHelper.BUILDINFO.osversion)
    val osArch = config.getString(ConfigHelper.BUILDINFO.osarch)
    val javaVendor = config.getString(ConfigHelper.BUILDINFO.javavendor)
    val javaVersion = config.getString(ConfigHelper.BUILDINFO.javaver)

    fun log() {
        CCLogger.info("== Build Info ==")
        CCLogger.info("Type: $releaseType")
        CCLogger.info("Version: ${SnipSniper.getVersionString()}")
        CCLogger.info("Build date: $buildDate")
        CCLogger.info("GitHash: $gitHash")
        CCLogger.info("GitHash Full: $gitHashFull")
        CCLogger.info("Branch: $branch")
        CCLogger.info("OS Name: $osName")
        CCLogger.info("OS Version: $osVersion")
        CCLogger.info("OS Arch: $osArch")
        CCLogger.info("Java Vendor: $javaVendor")
        CCLogger.info("Java Version: $javaVersion")
    }
}