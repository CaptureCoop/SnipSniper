package net.snipsniper.configwindow.tabs

import net.snipsniper.config.Config
import net.snipsniper.configwindow.ConfigWindow.PAGE

interface ITab {
    val page: PAGE?
    fun setup(configOriginal: Config?)
    var isDirty: Boolean
}