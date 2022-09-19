package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.Utils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.json.JSONObject
import java.awt.image.BufferedImage

class LangManager {
    //TODO: On first start use user.language property to check if we can use the users language
    companion object {
        private const val DEFAULT_LANGUAGE = "en"
        private const val MISSING_STRING_CHAR = "~"
        private const val FLAG_SIZE = 16
        private val flagCache = HashMap<String, BufferedImage>()
        private val langMap = HashMap<String, JSONObject>()
        val languages = ArrayList<String>()

        fun load() {
            CCLogger.log("Loading language files...")
            JSONObject(FileUtils.loadFileFromJar("lang/languages.json")).getJSONArray("languages").also { arr ->
                for (i in 0 until arr.length()) {
                    val content = FileUtils.loadFileFromJar("lang/${arr.getString(i)}.json")
                    val langID = arr.getString(i)
                    langMap[langID] = JSONObject(content)
                    languages.add(langID)
                }
            }
            CCLogger.log("Done!")
        }

        fun getJSON(language: String): JSONObject? = langMap[language]

        fun getItem(language: String, key: String): String {
            val preferred = langMap[Utils.replaceVars(language)]?.getJSONObject("strings")?.getString(key)
            val default = langMap[DEFAULT_LANGUAGE]?.getJSONObject("strings")?.getString(key)
            return preferred ?: default ?: "LM<$key>".also {
                CCLogger.log("Could not find key <$key> in language file <$language>", CCLogLevel.ERROR)
            }
        }

        fun getItem(key: String): String = getItem(SnipSniper.config.getString(ConfigHelper.MAIN.language), key)

        fun getFlag(language: String): BufferedImage {
            val file = langMap[language]?.getString("icon")
            val flag = ImageManager.getImage("flags/$file.png")

            return flagCache[language] ?: BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).also { img ->
                img.createGraphics().also { g ->
                    fun sz(s: Int): Int = FLAG_SIZE / 2 - s / 2
                    g.drawImage(flag, sz(flag.width), sz(flag.height), null)
                    g.dispose()
                }
                flagCache[language] = img
            }
        }

        fun getLanguage(): String = SnipSniper.config.getString(ConfigHelper.MAIN.language)
    }
}
