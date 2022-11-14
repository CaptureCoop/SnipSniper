package net.snipsniper.utils

import java.awt.Desktop
import java.net.URI

class Links {
    companion object {
        const val KOFI = "https://ko-fi.com/SvenWollinger"
        const val STABLE_VERSION_TXT = "https://raw.githubusercontent.com/SvenWollinger/SnipSniper/master/version.txt"
        const val STABLE_JAR = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper.jar"
        const val STABLE_INSTALLER = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper_Installer_Win.exe"
        const val STABLE_PORTABLE = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper_Portable_Win.zip"
        const val API_LATEST_COMMIT = "https://api.github.com/repos/capturecoop/SnipSniper/commits/master?per_page=1\n" //TODO: Can this \n be removed?
        const val DEV_JAR = "https://SnipSniper.net/jars/SnipSniper.jar"

        fun getURI(link: String): URI = URI(link)
        fun openLink(link: String) = Desktop.getDesktop().browse(getURI(link))
    }
}