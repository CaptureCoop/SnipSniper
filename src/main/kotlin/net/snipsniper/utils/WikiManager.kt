package net.snipsniper.utils

import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.json.JSONArray
import org.json.JSONObject

//TODO: Make object
class WikiManager {
    companion object {
        private val strings = HashMap<String, JSONObject>()

        fun load(language: String) {
            CCLogger.info("Loading wiki files...")
            val languagesJSON = FileUtils.loadFileFromJar("wiki/languages.json") ?: throw Exception("languages.json could not be loaded!")

            val languages = JSONArray(languagesJSON)
            var languageToLoad = "en"
            for(i in 0 until languages.length()) {
                languages.getString(i).also { cLang ->
                    if(language == cLang) languageToLoad = cLang
                }
            }
            finalLoad(languageToLoad)
            CCLogger.info("Done!")
        }

        private fun finalLoad(language: String) {
            val listString = FileUtils.loadFileFromJar("wiki/list.json") ?: throw Exception("Error loading list.json")

            val list = JSONArray(listString)
            for(i in 0 until list.length()) {
                list.getString(i).also { string ->
                    val json = FileUtils.loadFileFromJar("wiki/$language/$string") ?: throw Exception("Error with loading file $language $string")
                    strings[string] = JSONObject(json)
                }
            }
        }

        fun getContent(string: String): String {
            return strings[string]?.getString("content") ?: "key <$string> not found".also {
                CCLogger.log("WikiManager error: $string not found!", CCLogLevel.ERROR)
            }
        }
    }
}