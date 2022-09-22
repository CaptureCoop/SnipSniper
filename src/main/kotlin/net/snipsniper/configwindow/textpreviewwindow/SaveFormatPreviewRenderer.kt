package net.snipsniper.configwindow.textpreviewwindow

import net.snipsniper.utils.DrawUtils
import net.snipsniper.utils.Utils
import java.awt.*
import javax.swing.JPanel

class SaveFormatPreviewRenderer(width: Int, height: Int) : JPanel() {
    //TODO: Shouldnt this be in a config?
    lateinit var textPreviewWindow: TextPreviewWindow

    companion object {
        val DEFAULT_FORMAT = "%year%-%month%-%day%__%hour%_%minute%_%second%"
    }

    init {
        Dimension(width, height).also {
            preferredSize = it
            minimumSize = it
        }
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