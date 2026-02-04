package net.snipsniper.utils

import com.formdev.flatlaf.util.SystemFileChooser
import java.awt.Component
import java.io.File
import javax.swing.JFileChooser

object SnipFileChooser {
    val IMAGE_FILTER = Filter("Images", listOf("png", /*TODO: Re-Enable once working: "gif",*/ "jpg", "jpeg"))
    val IMAGE_FILTER_NO_GIFS = IMAGE_FILTER.getWithBlacklistedExtensions(listOf("gif"))

    data class Filter(
        val description: String,
        val extensions: List<String>
    ) {
        fun getWithBlacklistedExtensions(blacklist: List<String>): Filter {
            return Filter(
                description = description,
                extensions = extensions.filter { !blacklist.contains(it) }
            )
        }
    }

    enum class SelectionType(val swingType: Int, val nativeType: Int) {
        FILES_ONLY(JFileChooser.FILES_ONLY, SystemFileChooser.FILES_ONLY),
        DIRECTORY_ONLY(JFileChooser.DIRECTORIES_ONLY, SystemFileChooser.DIRECTORIES_ONLY),
        FILES_AND_DIRECTORIES(JFileChooser.FILES_AND_DIRECTORIES, SystemFileChooser.DIRECTORIES_ONLY)
    }

    fun saveSystemFileChooser(
        file: File = File(""),
        parent: Component? = null,
        fileFilters: List<Filter> = listOf()
    ): File? {
        val chooser = SystemFileChooser()
        chooser.selectedFile = file
        fileFilters.forEach { filter ->
            chooser.addChoosableFileFilter(SystemFileChooser.FileNameExtensionFilter(filter.description, *filter.extensions.toTypedArray()))
        }
        if (chooser.showSaveDialog(parent) == SystemFileChooser.APPROVE_OPTION) {
            return chooser.selectedFile
        }
        return null
    }

    fun openSystemImage(
        path: String = File("").absolutePath,
        parent: Component? = null
    ): File? {
        return openSystemFileChooser(
            path = path,
            parent = parent,
            type = SelectionType.FILES_ONLY,
            multiFileSelection = false,
            fileFilters = listOf(IMAGE_FILTER)
        )?.firstOrNull()
    }

    fun openSystemMultipleImages(
        path: String = File("").absolutePath,
        parent: Component? = null,
    ): Array<File>? {
        return openSystemFileChooser(
            path = path,
            parent = parent,
            type = SelectionType.FILES_ONLY,
            multiFileSelection = true,
            fileFilters = listOf(IMAGE_FILTER)
        )
    }

    fun openSystemFileChooser(
        path: String = File("").absolutePath,
        multiFileSelection: Boolean = false,
        parent: Component? = null,
        type: SelectionType = SelectionType.FILES_ONLY,
        fileFilters: List<Filter> = listOf()
    ): Array<File>? {
        val chooser = SystemFileChooser()
        chooser.currentDirectory = File(path)
        chooser.isMultiSelectionEnabled = multiFileSelection
        chooser.fileSelectionMode = type.nativeType
        fileFilters.forEach { filter ->
            chooser.addChoosableFileFilter(SystemFileChooser.FileNameExtensionFilter(filter.description, *filter.extensions.toTypedArray()))
        }
        if (chooser.showOpenDialog(parent) == SystemFileChooser.APPROVE_OPTION) {
            //TODO: is this neccessary? can we always return selectedFiles?
            return if(multiFileSelection) chooser.selectedFiles else arrayOf(chooser.selectedFile)
        }
        return null
    }
}