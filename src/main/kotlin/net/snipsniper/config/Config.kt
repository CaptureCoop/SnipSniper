package net.snipsniper.config

import net.snipsniper.SnipSniper
import net.snipsniper.utils.Utils
import org.capturecoop.cccolorutils.CCColor
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import java.io.*

class Config {
    private lateinit var filename: String
    private var settings = ConfigContainer()
    private var defaults = ConfigContainer()

    companion object {
        const val EXTENSION = "cfg"
        const val DOT_EXTENSION = ".$EXTENSION"
    }

    constructor(config: Config) {
        CCLogger.log("Copying config for <${config.filename}>", CCLogLevel.DEBUG)
        loadFromConfig(config)
    }

    constructor(filename: String, defaultFile: String) {
        loadFromDisk(filename, defaultFile, false)
    }

    constructor(filename: String, defaultFile: String, ignoreLocal: Boolean) {
        loadFromDisk(filename, defaultFile, ignoreLocal)
    }

    fun loadFromConfig(config: Config) {
        this.filename = config.filename
        settings.loadFromContainer(config.settings)
        defaults.loadFromContainer(config.defaults)
    }

    private fun loadFromDisk(filename: String, defaultFile: String, ignoreLocal: Boolean) {
        this.filename = filename
        val idPrefix = "$filename: "
        CCLogger.log("${idPrefix}Creating config object for <$filename>.", CCLogLevel.DEBUG)
        val defaultPath = "/net/snipsniper/resources/cfg/$defaultFile"
        if(!ignoreLocal) {
            val filePath = SnipSniper.configFolder + filename
            if(File(filePath).exists()) {
                CCLogger.log("${idPrefix}Config file found locally! Using that one.", CCLogLevel.DEBUG)
                loadFile(filePath, settings, false)
            } else {
                CCLogger.log("${idPrefix}Config file not found locally! Using default.", CCLogLevel.DEBUG)
                loadFile(defaultPath, settings, true)
            }
        } else {
            CCLogger.log("${idPrefix}ignoreLocal is true. Ignoring local file.", CCLogLevel.DEBUG)
        }
        loadFile(defaultPath, defaults, true)
        CCLogger.log(idPrefix + "Done!", CCLogLevel.DEBUG)
    }

    private fun loadFile(filename: String, container: ConfigContainer, inJar: Boolean) {
        val reader = if(inJar) BufferedReader(InputStreamReader(javaClass.getResourceAsStream(filename))) else BufferedReader(FileReader(filename))
        reader.lineSequence().forEach {
            if(it.startsWith("#")) {
                container.set(it)
            } else if(it.isEmpty() || it == " ") {
                container.addNewLine()
            } else if(it.contains("=")) {
                val args = it.split("=")
                container.set(args[0], args[1])
            }
        }
        reader.close()
    }

    private fun getSavePath(): String = "${SnipSniper.configFolder}/${filename}"

    //TODO: In the old config file we used way more checking if values are the type they are asking for, and if they are null
    //Make sure this works!
    fun getRawString(key: String): String {
        val def = "<NULL> ($key)"
        val result = settings.get(key) ?: defaults.get(key) ?: def
        if(result == def) CCLogger.log("No value found for <$key> in Config <${SnipSniper.configFolder + filename}>.", CCLogLevel.ERROR)
        return result
    }
    fun getRawString(key: Any): String = getRawString(key.toString())
    fun getString(key: String): String = Utils.replaceVars(getRawString(key))
    fun getString(key: Any): String = getString(key.toString())

    fun getInt(key: String): Int = getString(key).toInt()
    fun getInt(key: Any): Int = getInt(key.toString())

    fun getFloat(key: String): Float = getString(key).toFloat()
    fun getFloat(key: Any): Float = getFloat(key.toString())

    fun getBool(key: String): Boolean = getString(key).toBoolean()
    fun getBool(key: Any): Boolean = getBool(key.toString())

    fun getColor(key: String): CCColor = CCColor.fromSaveString(getString(key))
    fun getColor(key: Any): CCColor = getColor(key.toString())

    fun set(key: String, value: String) = settings.set(key, value)
    fun set(key: Any, value: String) = set(key.toString(), value)
    fun set(key: Any, value: Int) = set(key.toString(), value.toString())
    fun set(key: Any, value: Boolean) = set(key.toString(), value.toString())

    fun deleteFile() = File(getSavePath()).delete()

    fun save() {
        if(SnipSniper.isDemo) return
        saveFile(if(settings.isEmpty()) defaults else settings)
    }

    private fun saveFile(container: ConfigContainer) {
        val writer = BufferedWriter(FileWriter(getSavePath()))
        container.list.forEach { writer.write(it.toString() + "\n") }
        writer.close()
    }

    fun settingsEquals(other: Config): Boolean = settings == other.settings

    fun getFilename(): String = filename
}