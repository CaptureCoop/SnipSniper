package net.snipsniper.utils

import net.snipsniper.SnipSniper
import org.capturecoop.cclogger.CCLogLevel
import org.capturecoop.cclogger.CCLogger
import org.capturecoop.ccutils.utils.CCStringUtils
import java.awt.Desktop
import java.io.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class FileUtils {
    companion object {
        fun deleteRecursively(folder: String) = deleteRecursively(File(folder))
        fun deleteRecursively(folder: File) = folder.deleteRecursively()

        fun delete(file: String) = delete(File(file))

        fun delete(file: File): Boolean {
            if (!file.exists()) return true
            if (!file.delete()) {
                CCLogger.warn("File (${file.absolutePath}) could not be deleted!")
                return false
            }
            return true
        }

        fun mkdir(file: String) = mkdir(File(file))

        fun mkdir(folder: File): Boolean {
            if (folder.exists()) return true
            if (!folder.mkdir()) {
                CCLogger.warn("Folder (${folder.absolutePath}) could not be created.")
                return false
            }
            return true
        }

        fun mkdirs(file: String?) = mkdirs(File(file))

        fun mkdirs(folder: File): Boolean {
            if (folder.exists()) return true
            if (!folder.mkdirs()) {
                CCLogger.warn("Folders ($folder) could not be created.")
                return false
            }
            return true
        }

        fun mkdirs(vararg folders: String): Boolean {
            return mkdirs(*Array(folders.size) { i -> File(folders[i]) })
        }

        fun mkdirs(vararg folders: File): Boolean {
            var success = true
            for (folder in folders) if (!mkdirs(folder)) success = false
            return success
        }

        fun listFiles(folder: String) = listFiles(File(folder))

        fun listFiles(folder: File) = folder.listFiles()

        fun getFilesInFolders(path: String): ArrayList<String> {
            val result = ArrayList<String>()
            for (file in listFiles(path)) {
                if (file.isDirectory) result.addAll(getFilesInFolders(file.absolutePath))
                if (!file.isDirectory) result.add(CCStringUtils.correctSlashes(file.absolutePath))
            }
            return result
        }

        fun exists(vararg files: String) = exists(*Array(files.size) { i -> File(files[i]) })

        fun exists(vararg files: File): Boolean {
            var allExist = true
            for (file in files) {
                if (!file.exists()) allExist = false
            }
            return allExist
        }

        fun openFolder(path: String) {
            try {
                Desktop.getDesktop().open(File(path))
            } catch (ioException: IOException) {
                CCLogger.error("Could not open folder \"$path\"!")
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
            }
        }

        fun printFile(filename: String, text: String?) {
            try {
                PrintWriter(filename).also {
                    it.print(text)
                    it.close()
                }
            } catch (fileNotFoundException: FileNotFoundException) {
                CCLogger.error("Could not write to file \"$filename\"!")
                CCLogger.logStacktrace(fileNotFoundException, CCLogLevel.ERROR)
            }
        }

        fun getFileExtension(file: File): String {
            return getFileExtension(file, true)
        }

        fun getFileExtension(file: File, dot: Boolean): String {
            val name = file.name
            var lastIndexOf = name.lastIndexOf(".")
            if (lastIndexOf == -1) {
                return "" // empty extension
            }
            if (!dot) lastIndexOf++
            return name.substring(lastIndexOf)
        }

        fun getCanonicalPath(path: String): String? {
            try {
                return File(path).canonicalPath
            } catch (ioException: IOException) {
                CCLogger.error("Could not get path for \"$path\"!")
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
            }
            return null
        }

        fun getJarFolder(): String {
            URLDecoder.decode(Paths.get(SnipSniper::class.java.protectionDomain.codeSource.location.toURI()).toString(), "UTF-8")!!.also { fullPath ->
                File(fullPath).also { file ->
                    return if(file.isFile) fullPath.replace(file.name, "") else fullPath
                }
            }
        }

        fun copyFromJar(jarPath: String, path: String): Boolean {
            if (jarPath.startsWith("\\") || jarPath.startsWith("//"))
                CCLogger.warn("jarPath ($path) is starting with slashes, this generally does not work inside the jar!")
            if (exists(path))
                delete(path)
            val inputStream = ClassLoader.getSystemResourceAsStream(jarPath)
            if (inputStream == null) {
                CCLogger.error("InputStream is null! Copying failed! jarPath: $jarPath, path: $path")
                return false
            }
            try {
                Files.copy(inputStream, File(path).canonicalFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            } catch (ioException: IOException) {
                CCLogger.error("Issue copying from jar!")
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
                return false
            }
            return true
        }

        fun loadFileFromJar(file: String): String? {
            val content = StringBuilder()
            try {
                val path = "net/snipsniper/resources/$file"
                val inputStream = ClassLoader.getSystemResourceAsStream(path)
                if(inputStream == null) {
                    CCLogger.error("Could not load file $path from jar!")
                    return null
                }
                val streamReader = InputStreamReader(inputStream, StandardCharsets.UTF_8)
                BufferedReader(streamReader).also {
                    it.readLines().forEach { line -> content.append(line) }
                    it.close()
                }
                streamReader.close()
            } catch (ioException: IOException) {
                CCLogger.error("Could not load file: $file")
                CCLogger.logStacktrace(ioException, CCLogLevel.ERROR)
            }
            return content.toString()
        }
    }
}