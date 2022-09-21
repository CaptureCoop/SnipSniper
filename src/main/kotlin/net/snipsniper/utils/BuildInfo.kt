package net.snipsniper.utils

import net.snipsniper.config.Config
import net.snipsniper.config.ConfigHelper

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
}