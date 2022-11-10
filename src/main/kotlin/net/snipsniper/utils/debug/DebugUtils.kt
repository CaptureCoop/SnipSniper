package net.snipsniper.utils.debug

import net.snipsniper.LangManager
import net.snipsniper.utils.FileUtils
import java.io.File

class DebugUtils {
    companion object {
        fun jsonLang() {
            println("Creating language debug files under \\lang\\...\n")
            if(!File("lang").mkdir() && !File("lang").exists()) {
                println("Could not create folder. Aborting")
                return
            }
            LangManager.load()

            val en = LangManager.getJSON("en") ?: throw Exception("JSON not found!")
            FileUtils.printFile("lang/en.json", en.toString())

            LangManager.languages.forEach {
                var successful = true
                val strings = en.getJSONObject("strings")
                val keys = strings.keys()
                while(keys.hasNext()) {
                    val key = keys.next()
                    val obj = LangManager.getJSON(it)!!.getJSONObject("strings")
                    if(!obj.has(key)) {
                        successful = false
                        println("$it.json is missing <$key>")
                        obj.put(key, "<MISSING>")
                    }
                }
                if(!successful) {
                    FileUtils.printFile("lang/$it.json", LangManager.getJSON(it).toString())
                    println("Missing lines found for $it.json\n")
                }
            }
            println("Done. If no issues were reported you are golden :^)")
        }
    }
}