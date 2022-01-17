package net.snipsniper.scviewer;

import net.snipsniper.utils.Utils;
import net.snipsniper.snipscope.SnipScopeRenderer;
import net.snipsniper.snipscope.SnipScopeWindow;

import java.awt.*;

public class SCViewerRenderer extends SnipScopeRenderer {
    private final RenderingHints qualityHints = Utils.getRenderingHints();

    public SCViewerRenderer(SnipScopeWindow snipScopeWindow) {
        super(snipScopeWindow);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(qualityHints);
        super.paint(g2d);
    }
}
