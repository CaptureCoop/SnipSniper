package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.json.JSONObject
import java.awt.image.BufferedImage

class LangManager {
    companion object {
        const val DEFAULT_LANGUAGE = "en"
        const val MISSING_STRING_CHAR = "~"
        val languages = ArrayList<String>()
        val langMap = HashMap<String, JSONObject>()

        fun load() {
            CCLogger.log("Loading language files...")
            val langs = JSONObject(FileUtils.loadFileFromJar("lang/languages.json")).getJSONArray("languages")
            for (i in 0 until langs.length()) {
                val content = FileUtils.loadFileFromJar("lang/${langs.getString(i)}.json")
                val langID = langs.getString(i)
                langMap[langID] = JSONObject(content)
                languages.add(langID)
            }
            CCLogger.log("Done!")
        }

        fun getJSON(language: String): JSONObject? = langMap[language]

        fun getItem(language: String, key: String): String {
            val strings = langMap[language]?.getJSONObject("strings")
            val stringsDefault = langMap[DEFAULT_LANGUAGE]?.getJSONObject("strings")
            if (strings != null && strings.has(key))
                return strings.getString(key)
            else if (stringsDefault != null && stringsDefault.has(key))
                return stringsDefault.getString(key)

            CCLogger.log("Could not find key <$key> in language file <$language>", CCLogLevel.ERROR)
            return "LM<$key>"
        }

        fun getItem(key: String): String = getItem(SnipSniper.config.getString(ConfigHelper.MAIN.language), key)

        fun getIcon(language: String): BufferedImage {
            val file = langMap[language]?.getString("icon")
            val flag = ImageManager.getImage("flags/$file.png")
            val size = 16

            val icon = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
            val g = icon.createGraphics()
            g.drawImage(flag, size / 2 - flag.width / 2, size / 2 - flag.height / 2, null)
            g.dispose()
            return icon
        }

        fun getLanguage(): String = SnipSniper.config.getString(ConfigHelper.MAIN.language)
    }
}
