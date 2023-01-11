package net.snipsniper

import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.FileUtils
import net.snipsniper.utils.ImageUtils
import net.snipsniper.utils.Utils
import org.capturecoop.cclogger.CCLogger
import org.json.JSONObject
import java.awt.image.BufferedImage

object LangManager {
    const val DEFAULT_LANGUAGE = "en"
    private const val FLAG_SIZE = 16
    private val flagCache = HashMap<String, BufferedImage>()
    private val langMap = HashMap<String, JSONObject>()
    val languages = ArrayList<String>()

    fun load() {
        CCLogger.info("Loading language files...")
        JSONObject(FileUtils.loadFileFromJar("lang/languages.json")).getJSONArray("languages").also { arr ->
            for (i in 0 until arr.length()) {
                arr.getString(i).also {
                    langMap[it] = JSONObject(FileUtils.loadFileFromJar("lang/${arr.getString(i)}.json"))
                    languages.add(it)
                }
            }
        }
        CCLogger.info("Done!")
    }

    fun getJSON(language: String): JSONObject? = langMap[language]

    fun getItem(language: String, key: String): String {
        val preferred = langMap[Utils.replaceVars(language)]?.getJSONObject("strings")?.getString(key)
        val default = langMap[DEFAULT_LANGUAGE]?.getJSONObject("strings")?.getString(key)
        return preferred ?: default ?: "LM<$key>".also {
            CCLogger.error("Could not find key <$key> in language file <$language>")
        }
    }

    fun getItem(key: String): String = getItem(SnipSniper.config.getString(ConfigHelper.MAIN.language), key)

    fun getFlag(language: String): BufferedImage {
        val file = langMap[language]?.getString("icon")
        val flag = ImageManager.getImage("flags/$file.png")

        return flagCache[language] ?: ImageUtils.newBufferedImage(16, 16) {
            fun sz(s: Int): Int = FLAG_SIZE / 2 - s / 2
            it.drawImage(flag, sz(flag.width), sz(flag.height), null)
        }.also { flagCache[language] = it }
    }

    fun getLanguage(): String = SnipSniper.config.getString(ConfigHelper.MAIN.language)
}
