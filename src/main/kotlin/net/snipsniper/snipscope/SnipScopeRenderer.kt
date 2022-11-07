package net.snipsniper.snipscope

import net.snipsniper.utils.Utils
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import javax.swing.JPanel

open class SnipScopeRenderer(private val snipScopeWindow: SnipScopeWindow): JPanel() {
    private val qualityHints = Utils.getRenderingHints()
    private val zoomAntialisingKickIn: Double = 2.0
    lateinit var lastRectangle: Rectangle

    override fun paint(g: Graphics) {
        g as Graphics2D
        if (snipScopeWindow.zoom < zoomAntialisingKickIn)
            g.setRenderingHints(qualityHints)

        val optimalDimension = snipScopeWindow.optimalImageDimension
        val image = snipScopeWindow.image
        if (image != null && optimalDimension != null) {
            var x = width / 2 - optimalDimension.width / 2
            var y = height / 2 - optimalDimension.height / 2

            x -= snipScopeWindow.zoomOffset.x
            y -= snipScopeWindow.zoomOffset.y

            val posModifier = snipScopeWindow.position
            x -= posModifier.x
            y -= posModifier.y

            val zoom = snipScopeWindow.zoom
            lastRectangle = Rectangle(x, y, (optimalDimension.width * zoom).toInt(), (optimalDimension.height * zoom).toInt())
            g.drawImage(image, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this)
            g.setColor(Color.BLACK)
            //TODO: add config option for outline
            g.drawRect(lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height)
        }
    }

    fun renderUI(g: Graphics2D) {
        snipScopeWindow.uiComponents.forEach { it.render(g) }
    }
}