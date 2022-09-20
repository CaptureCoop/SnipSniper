package net.snipsniper.utils

import com.erigir.mslinks.ShellLink
import net.snipsniper.LangManager
import net.snipsniper.SnipSniper
import org.apache.commons.lang3.SystemUtils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCStringUtils
import org.json.JSONObject
import java.awt.*
import java.awt.event.ItemEvent
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.URL
import javax.swing.ImageIcon
import javax.swing.JComboBox
import javax.swing.JFrame
import javax.swing.JOptionPane
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min

class Utils {
    companion object {
        fun getPlatformType(string: String?): PlatformType {
            if(string == null || string.isEmpty()) return PlatformType.JAR

            return when(string.lowercase()) {
                "jar" -> PlatformType.JAR
                "win" -> PlatformType.WIN
                "win_installed" -> PlatformType.WIN_INSTALLED
                "steam" -> PlatformType.STEAM
                else -> PlatformType.UNKNOWN
            }
        }

        fun getReleaseType(string: String): ReleaseType {
            return when(string.lowercase()) {
                "release", "stable" -> ReleaseType.STABLE
                "dev" -> ReleaseType.DEV
                "dirty" -> ReleaseType.DIRTY
                else -> ReleaseType.UNKNOWN
            }
        }

        fun getLaunchType(string: String?): LaunchType {
            if(string == null) return LaunchType.NORMAL
            return when(string.lowercase()) {
                "editor" -> LaunchType.EDITOR
                "viewer" -> LaunchType.VIEWER
                else -> LaunchType.NORMAL
            }
        }

        fun replaceVars(string: String): String {
            var result = string.replace("%username%", System.getProperty("user.name"))
            result = result.replace("%userprofile%", System.getProperty("user.home"))
            result = result.replace("%userlang%", System.getProperty("user.language"))
            return result
        }

        fun getShortGitHash(longHash: String): String = longHash.substring(0, 7)

        fun getTextFromWebsite(url: String): String? = getTextFromWebsite(URL(url))

        private fun getTextFromWebsite(url: URL): String? {
            val result = StringBuilder()
            try {
                BufferedReader(InputStreamReader(url.openStream())).also { reader ->
                    reader.lineSequence().forEach { result.append(it) }
                }
            } catch(e: Exception) {
                CCLogger.log("Error requesting content from website (${url}): ${e.message}", CCLogLevel.ERROR)
                return null
            }
            return result.toString()
        }

        fun getHashFromAPI(link: String): String? {
            val text = getTextFromWebsite(link) ?: return null
            return JSONObject(text).getString("sha")
        }

        fun getRenderingHints(): RenderingHints {
            return RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON).also {
                it[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
            }
        }

        fun getDisabledColor(): Color = Color(128, 128, 128, 100)

        //https://stackoverflow.com/a/10245583
        private fun getScaledDimension(toFit: Dimension, boundary: Dimension): Dimension {
            val originalWidth = toFit.width
            val originalHeight = toFit.height
            val boundWidth = boundary.width
            val boundHeight = boundary.height
            var newWidth = originalWidth
            var newHeight = originalHeight

            if (originalWidth > boundWidth) {
                newWidth = boundWidth //scale width to fit
                newHeight = (newWidth * originalHeight) / originalWidth //scale height to maintain aspect ratio
            }
            // then check if we need to scale even with the new height
            if (newHeight > boundHeight) {
                newHeight = boundHeight //scale height to fit instead
                newWidth = (newHeight * originalWidth) / originalHeight //scale width to maintain aspect ratio
            }
            return Dimension(newWidth, newHeight)
        }

        fun getScaledDimension(image: BufferedImage, boundary: Dimension): Dimension = getScaledDimension(Dimension(image.width, image.height), boundary)

        fun executeProcess(waitTillDone: Boolean, vararg args: String) {
            ProcessBuilder(*args).start().also { if(waitTillDone) it.waitFor() }
        }

        fun containsRectangleFully(rect: Rectangle, contains: Rectangle): Boolean = (contains.x + contains.width) < (rect.x + rect.width) && (contains.x) > (rect.x) && (contains.y) > (rect.y) && (contains.y + contains.height) < (rect.y + rect.height)

        fun fixRectangle(rect: Rectangle): Rectangle {
            return Rectangle().also {
                it.x = min(rect.x, rect.width)
                it.y = min(rect.y, rect.height)
                it.width = max(rect.x, rect.width)
                it.height = max(rect.y, rect.height)
            }
        }

        //TODO: This should probably have a better name, since this just prepares screenshots no?
        fun constructFilename(format: String, modifier: String): String {
            var filename = CCStringUtils.formatDateTimeString(format)
            filename = filename.replace("%random%", CCStringUtils.getRandomString(10, true, true))
            return "${filename}${modifier}.png"
        }

        //https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
        fun restartApplication(vararg args: String): Boolean {
            //TODO: Check if this actually uses the supplied jdk if we use a differnet one
            val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
            //TODO: better way to locate jar? Also put inside SnipSniper.kt
            val currentJar = File((SnipSniper::class as Any).javaClass.protectionDomain.codeSource.location.toURI())
            if(!currentJar.name.endsWith(".jar")) return false

            val command = arrayOf(javaBin, "-jar", currentJar.path, "-r", *args)
            executeProcess(false, *command)
            SnipSniper.exit(true)
            return true
        }

        fun showPopup(parent: Component, message: String, title: String, optionType: Int, messageType: Int, icon: BufferedImage, blockScreenshot: Boolean): Int {
            //TODO: Is this correct? This is how it was in the java file...
            if(blockScreenshot) SnipSniper.isIdle = false
            val result = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType, ImageIcon(icon.scaled(16, 16)))
            if(blockScreenshot) SnipSniper.isIdle = true
            return result
        }

        fun getSystemVersion(): String {
            if(!SystemUtils.IS_OS_WINDOWS) return System.getProperty("os.version")
            Runtime.getRuntime().exec("cmd.exe /c ver").also {
                BufferedReader(InputStreamReader(it.inputStream)).also { reader ->
                    var output = ""
                    reader.readLines().forEach {l -> if(l.isNotEmpty()) output += l}
                    return Regex("(?<=\\[)(.*?)(?=])").find(output)?.value?.lowercase()?.replace("version ", "") ?: output
                }
            }
        }

        fun getLanguageDropdown(selectedLanguage: String, onSelect: IFunction): JComboBox<DropdownItem> {
            val langItems = ArrayList<DropdownItem>()
            var selectedItem: DropdownItem? = null
            LangManager.languages.forEach {
                DropdownItem(LangManager.getItem(it, "lang_$it"), it, LangManager.getFlag(it)).also { item ->
                    langItems.add(item)
                    if(it == selectedLanguage) selectedItem = item
                }
            }
            return JComboBox(langItems.toTypedArray()).also {
                it.renderer = DropdownItemRenderer(langItems.toTypedArray())
                it.selectedItem = selectedItem
                it.addItemListener { event ->
                    if(event.stateChange == ItemEvent.SELECTED) {
                        val item = it.selectedItem as DropdownItem
                        onSelect.run(item.id)
                    }
                }
            }
        }

        fun createShellLink(linkLocation: String, originalLocation: String, icon: String) {
            ShellLink.createLink(originalLocation).also {
                it.iconLocation = icon
                it.saveTo(linkLocation)
            }
        }

        fun getGraphicsConfiguration(x: Int, y: Int): GraphicsConfiguration {
            //TODO: Look for better way of getting this, also we might not need to dispose af ter getting graphicsConfiguration
            JFrame().also { jf ->
                jf.isUndecorated = true
                jf.location = Point(x, y)
                jf.isVisible = true
                return jf.graphicsConfiguration.also { jf.dispose() }
            }
        }
    }

}