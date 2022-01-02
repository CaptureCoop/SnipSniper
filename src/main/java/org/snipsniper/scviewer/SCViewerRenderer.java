package org.snipsniper.scviewer;

import org.snipsniper.snipscope.SnipScopeRenderer;
import org.snipsniper.snipscope.SnipScopeWindow;
import org.snipsniper.utils.Utils;

import java.awt.*;

public class SCViewerRenderer extends SnipScopeRenderer {
    private final RenderingHints qualityHints;

    public SCViewerRenderer(SnipScopeWindow snipScopeWindow) {
        super(snipScopeWindow);
        qualityHints = Utils.getRenderingHints();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D)g;
        g2d.setRenderingHints(qualityHints);
        super.paint(g2d);
    }
}
