package net.snipsniper.utils

import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.geom.Rectangle2D


class DrawUtils {
    companion object {
        fun drawRect(g: Graphics, rect: Rectangle) = g.drawRect(rect.x, rect.y, rect.width, rect.height)
        fun fillRect(g: Graphics, rect: Rectangle) = g.fillRect(rect.x, rect.y, rect.width, rect.height)
        fun drawRect(g: Graphics2D, rect: Rectangle) = g.drawRect(rect.x, rect.y, rect.width, rect.height)
        fun fillRect(g: Graphics2D, rect: Rectangle) = g.fillRect(rect.x, rect.y, rect.width, rect.height)

        fun pickOptimalFontSize(g: Graphics2D, title: String, width: Int, height: Int): Int {
            var rect: Rectangle2D
            var fontSize = height
            do {
                fontSize--
                Font("Arial", Font.PLAIN, fontSize).also {
                    rect = getStringBoundsRectangle2D(g, title, it)
                }
            } while (rect.width >= width || rect.height >= height)
            return fontSize
        }

        fun getStringBoundsRectangle2D(g: Graphics, title: String, font: Font): Rectangle2D {
            g.font = font
            return g.fontMetrics.getStringBounds(title, g)
        }

        fun drawCenteredString(g: Graphics, text: String, rect: Rectangle, font: Font) {
            val metrics = g.getFontMetrics(font)
            val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
            val y = rect.y + ((rect.height - metrics.height) / 2) + metrics.ascent
            g.font = font
            g.drawString(text, x, y)
        }
    }
}