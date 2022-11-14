package net.snipsniper.configwindow

import net.snipsniper.ImageManager
import net.snipsniper.sceditor.stamps.IStamp
import net.snipsniper.utils.Utils
import org.capturecoop.ccutils.math.CCVector2Int
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import javax.swing.JPanel

class StampJPanel(stamp: IStamp, private var background: BufferedImage? = null, private var margin: Int = 0): JPanel() {
    private val renderingHints = Utils.getRenderingHints()
    var backgroundEnabled = true
        set(value) {
            field = value
            repaint()
        }

    var stamp = stamp
        set(value) {
            field = value
            repaint()
        }

    override fun paint(g: Graphics) {
        super.paint(g)
        g.color = getBackground()
        g.fillRect(0, 0, width, height)

        val g2d = g as Graphics2D
        g2d.setRenderingHints(renderingHints)
        if(backgroundEnabled) {
            background?.let { bg ->
                if(!(bg.width >= width && bg.height >= height))
                    background = ImageManager.getCodePreview()
                val pos = margin / 2
                val width = width - margin + margin / 2
                val height = height - margin + margin / 2
                g2d.drawImage(background, pos, pos, width, height, pos, pos, width, height, null)
                g2d.color.also { oldColor ->
                    g2d.color = Color.BLACK
                    g2d.drawRect(pos, pos, width - margin / 2, height - margin / 2)
                    g2d.color = oldColor
                }
            }
        }
        if(!isEnabled) g2d.color = Utils.getDisabledColor()
        g2d.drawRect(0,0, width - 1, height -1)

        stamp.render(g2d, null, CCVector2Int(width / 2, height / 2), doubleArrayOf(1.0, 1.0).toTypedArray(), false, false, 0)

        if(!isEnabled) g2d.fillRect(0, 0, width, width / 2)
        g2d.dispose()
    }
}