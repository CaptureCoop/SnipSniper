package net.snipsniper.configwindow

import net.snipsniper.SnipSniper
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.*
import org.capturecoop.cclogger.CCLogger

class UpdateButton: IDJButton("") {
    private val STATE_WAITING = "waiting"
    private val STATE_DOUPDATE = "update"
    private val STATE_IDLE = "idle"
    private val roundArrows = "icons/roundarrows.png".getImage().scaled(16, 16)
    private val checkmark = "icons/checkmark.png".getImage().scaled(16, 16)
    private val download = "icons/download.png".getImage().scaled(16, 16)

    init {
        id = STATE_WAITING
        text = "Check for update"
        icon = roundArrows.toImageIcon()

        addActionListener {
            if (id == STATE_DOUPDATE) {
                UpdateUtils.update()
                return@addActionListener
            }
            val bi = SnipSniper.buildInfo
            val version = bi.version
            val updateChannel = Utils.getReleaseType(SnipSniper.config.getString(ConfigHelper.MAIN.updateChannel))
            val isJar = SnipSniper.platformType === PlatformType.JAR
            val isDev = bi.releaseType === ReleaseType.DEV
            val isStable = bi.releaseType === ReleaseType.STABLE
            val isJarAndDev = isDev && isJar
            val isJarAndDevButStableBranch = updateChannel === ReleaseType.STABLE && !isStable && isJar
            println(isJarAndDev)
            if (isJarAndDev) {
                if (id == STATE_WAITING) {
                    text = "Checking for update..."
                    val newestHash = Utils.getShortGitHash(Utils.getHashFromAPI(Links.API_LATEST_COMMIT)!!)
                    if (newestHash.isEmpty()) {
                        text = "Error - No connection"
                        id = STATE_WAITING
                        icon = roundArrows.toImageIcon()
                    } else if (newestHash == bi.gitHash) {
                        text = "Up to date!"
                        id = STATE_IDLE
                        icon = checkmark.toImageIcon()
                    } else {
                        text = "<html><p align='center'>Update available! ($newestHash)</p></html>"
                        id = STATE_DOUPDATE
                        icon = download.toImageIcon()
                    }
                }
            } else if (isJarAndDevButStableBranch) {
                if (id == STATE_WAITING) {
                    text = "<html><p align='center'>Switch to stable</p></html>"
                    id = STATE_DOUPDATE
                }
            } else {
                if (id == STATE_WAITING) {
                    text = "Checking for update..."
                    val versionString = Utils.getTextFromWebsite(Links.STABLE_VERSION_TXT)
                    val onlineVersion = Version(versionString!!)
                    if (versionString.isEmpty()) {
                        text = "Error - No connection"
                        id = STATE_WAITING
                        icon = roundArrows.toImageIcon()
                    } else if (onlineVersion == version || version.isNewerThan(onlineVersion)) {
                        text = "Up to date!"
                        id = STATE_IDLE
                        icon = checkmark.toImageIcon()
                    } else if (onlineVersion.isNewerThan(version)) {
                        if (SnipSniper.platformType === PlatformType.STEAM) {
                            text = "<html><p align='center'>Update available! (${onlineVersion.digitsToString()})</p><p align='center'>Check Steam to update!</p></html>"
                            id = STATE_IDLE
                        } else {
                            text = "<html><p align='center'>Update available! (${onlineVersion.digitsToString()})</p></html>"
                            id = STATE_DOUPDATE
                        }
                        icon = download.toImageIcon()
                    } else {
                        text = "Error. Check console."
                        CCLogger.error("Issue checking for updates. Our Version: ${version.digitsToString()}, Online version: ${onlineVersion.digitsToString()}")
                        id = STATE_IDLE
                        icon = null
                    }
                }
            }
        }
    }
}