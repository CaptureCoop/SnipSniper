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

        fun getContrastColor(color: Color): Color {
            val y = (299f * color.red + 587 * color.green + 114 * color.blue) / 1000
            return if(y >= 128) Color.black else Color.white
        }

        fun getTextFromWebsite(url: String): String? = getTextFromWebsite(URL(url))

        fun getTextFromWebsite(url: URL): String? {
            val result = StringBuilder()
            try {
                val reader = BufferedReader(InputStreamReader(url.openStream()))
                reader.lineSequence().forEach { result.append(it) }
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
            val hints = RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            hints[RenderingHints.KEY_RENDERING] = RenderingHints.VALUE_RENDER_QUALITY
            return hints
        }

        fun getDisabledColor(): Color = Color(128, 128, 128, 100)

        //https://stackoverflow.com/a/10245583
        fun getScaledDimension(toFit: Dimension, boundary: Dimension): Dimension {
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

        fun rgb2hex(color: Color): String = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
        fun hex2rgb(hex: String): Color = Color(Integer.valueOf(hex.substring(1, 3), 16), Integer.valueOf(hex.substring(3, 5), 16), Integer.valueOf(hex.substring(5, 7), 16))

        fun executeProcess(waitTillDone: Boolean, vararg args: String) {
            val process = ProcessBuilder(*args).start()
            if(waitTillDone) process.waitFor()
        }

        fun containsRectangleFully(rect: Rectangle, contains: Rectangle): Boolean = (contains.x + contains.width) < (rect.x + rect.width) && (contains.x) > (rect.x) && (contains.y) > (rect.y) && (contains.y + contains.height) < (rect.y + rect.height)

        fun fixRectangle(rect: Rectangle): Rectangle {
            val result = Rectangle()
            result.x = min(rect.x, rect.width)
            result.y = min(rect.y, rect.height)
            result.width = max(rect.x, rect.width)
            result.height = max(rect.y, rect.height)
            return result
        }

        //TODO: This should probably have a better name, since this just prepares screenshots no?
        fun constructFilename(format: String, modifier: String): String {
            var filename = CCStringUtils.formatDateTimeString(format)
            filename = filename.replace("%random%", CCStringUtils.getRandomString(10, true, true))
            return "${filename}${modifier}.png"
        }

        //https://stackoverflow.com/questions/4159802/how-can-i-restart-a-java-application
        fun restartApplication(vararg args: String): Boolean {
            val javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java"
            //TODO: better way to locate jar? Also put inside SnipSniper.kt
            val currentJar = File((SnipSniper::class as Any).javaClass.protectionDomain.codeSource.location.toURI())

            if(!currentJar.name.endsWith(".jar")) return false

            val command = ArrayList<String>()
            command.add(javaBin)
            command.add("-jar")
            command.add(currentJar.path)
            command.add("-r")
            command.addAll(args)

            executeProcess(false, *command.toTypedArray())
            SnipSniper.exit(true)
            return true
        }

        fun showPopup(parent: Component, message: String, title: String, optionType: Int, messageType: Int, icon: BufferedImage, blockScreenshot: Boolean): Int {
            //TODO: Is this correct? This is how it was in the java file...
            if(blockScreenshot) SnipSniper.isIdle = false
            val result = JOptionPane.showConfirmDialog(parent, message, title, optionType, messageType, ImageIcon(icon.getScaledInstance(32, 32, 0)));
            if(blockScreenshot) SnipSniper.isIdle = true
            return result
        }

        fun getLanguageDropdown(selectedLanguage: String, onSelect: IFunction): JComboBox<DropdownItem> {
            val langItems = ArrayList<DropdownItem>()
            //TODO: Does this do anything?
            LangManager.languages.sort()
            var selectedItem: DropdownItem? = null
            LangManager.languages.forEach {
                val translated = LangManager.getItem(it, "lang_$it")
                val item = DropdownItem(translated, it, LangManager.getIcon(it))
                langItems.add(item)
                if(it == selectedLanguage) selectedItem = item
            }
            val dropdown = JComboBox(langItems.toTypedArray())
            dropdown.renderer = DropdownItemRenderer(langItems.toTypedArray())
            dropdown.selectedItem = selectedItem
            dropdown.addItemListener {
                if(it.stateChange == ItemEvent.SELECTED) {
                    val item = dropdown.selectedItem as DropdownItem
                    onSelect.run(item.id)
                }
            }
            return dropdown
        }

        fun createShellLink(linkLocation: String, originalLocation: String, icon: String) {
            val sl = ShellLink.createLink(originalLocation)
            sl.iconLocation = icon
            sl.saveTo(linkLocation)
        }
    }

}