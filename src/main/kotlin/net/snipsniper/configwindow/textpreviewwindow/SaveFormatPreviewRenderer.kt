package net.snipsniper.configwindow.textpreviewwindow

import net.snipsniper.SnipSniper
import net.snipsniper.config.ConfigHelper
import net.snipsniper.utils.DrawUtils
import net.snipsniper.utils.Utils
import java.awt.*
import javax.swing.JPanel

class SaveFormatPreviewRenderer(width: Int, height: Int) : JPanel() {
    lateinit var textPreviewWindow: TextPreviewWindow

    init {
        Dimension(width, height).also {
            preferredSize = it
            minimumSize = it
        }
    }

    companion object {
        val DEFAULT_FORMAT = SnipSniper.defaultProfileConfig.getString(ConfigHelper.PROFILE.saveFormat)
    }

    override fun paint(g: Graphics) {
        g.color = Color.LIGHT_GRAY
        g.fillRect(0, 0, width, height)
        g.color = Color.BLACK
        var raw = textPreviewWindow.text
        if(raw.isEmpty())
            raw = DEFAULT_FORMAT
        val text = Utils.constructFilename(raw, "")
        val margin = 100
        val font = Font("Arial", Font.BOLD, DrawUtils.pickOptimalFontSize(g as Graphics2D, text, width - margin, height))
        DrawUtils.drawCenteredString(g, text, Rectangle(0, 0, width, height), font)
    }
}