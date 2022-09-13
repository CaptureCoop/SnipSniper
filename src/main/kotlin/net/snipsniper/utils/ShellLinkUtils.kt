package net.snipsniper.utils

import com.erigir.mslinks.ShellLink

class ShellLinkUtils {
    companion object {
        fun createShellLink(linkLocation: String, originalLocation: String, icon: String) {
            val sl = ShellLink.createLink(originalLocation)
            sl.iconLocation = icon
            sl.saveTo(linkLocation)
        }
    }
}