package net.snipsniper.utils

import org.capturecoop.defaultdepot.files.FileHandle
import java.awt.Font

object FontLoader {
    val defaultFont: Font = loadFont(FileHandle.internal("/net/snipsniper/resources/fonts/AtkinsonHyperlegibleNext-Regular.otf"))

    fun loadFont(handle: FileHandle): Font {
        return Font.createFont(Font.TRUETYPE_FONT, handle.getInputStream())
    }
}