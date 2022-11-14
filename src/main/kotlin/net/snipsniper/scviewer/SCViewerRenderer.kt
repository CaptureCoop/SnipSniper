package net.snipsniper.scviewer

import net.snipsniper.snipscope.SnipScopeRenderer
import net.snipsniper.snipscope.SnipScopeWindow
import net.snipsniper.utils.Utils
import java.awt.Graphics
import java.awt.Graphics2D

class SCViewerRenderer(snipSCopeWindow: SnipScopeWindow): SnipScopeRenderer(snipSCopeWindow) {
    private val qualityHints = Utils.getRenderingHints()

    override fun paint(g: Graphics) {
        g as Graphics2D
        g.setRenderingHints(qualityHints)
        super.paint(g)
    }
}