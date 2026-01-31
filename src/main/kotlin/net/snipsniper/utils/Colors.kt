package net.snipsniper.utils

import org.capturecoop.colorcomposer.ColorUtils
import java.awt.Color

object Colors {
    val AFFE00: Color = ColorUtils.hex2rgb("#affe00")!!
    val TRANSPARENT: Color = Color(0, 0, 0, 0)
    val RANDOM_OPAQUE: Color
        get() = Color(r(), r(), r(), 255)

    private fun r(): Int = (0..255).random()
}