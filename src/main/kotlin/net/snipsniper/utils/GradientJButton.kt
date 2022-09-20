package net.snipsniper.utils

import org.capturecoop.cccolorutils.CCColor
import java.awt.*
import javax.swing.JButton

class GradientJButton(val title: String, val color: CCColor) : JButton(title) {
    init {
        isContentAreaFilled = false
    }

    override fun paint(g: Graphics) {
        (g as Graphics2D).also { g2 ->
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g2.paint = color.getGradientPaint(width, height)
            if(!isEnabled)
                g2.paint = Utils.getDisabledColor()
            g2.fillRect(0, 0, width, height)
            g2.color = Color.BLACK
            g2.drawRect(0, 0, width - 1, height - 1)
            val drawHeight = (height / 1.5F).toInt()
            g2.font = Font(font.fontName, Font.PLAIN, drawHeight)

            val frc = g2.fontRenderContext
            val gv = g2.font.createGlyphVector(frc, text)
            val shape = gv.outline

            val oldTransform = g2.transform
            g2.translate(width / 2 - g2.fontMetrics.stringWidth(title) / 2, height / 2 + drawHeight / 3)
            g2.stroke = BasicStroke(2F)
            g2.draw(shape)
            g2.color = Color.WHITE
            g2.fill(shape)
            g2.transform = oldTransform

            if(!isEnabled) {
                g2.color = Utils.getDisabledColor()
                g2.fillRect(0, 0, width, height)
            }

            g2.dispose()
            super.paint(g2)
        }
    }
}