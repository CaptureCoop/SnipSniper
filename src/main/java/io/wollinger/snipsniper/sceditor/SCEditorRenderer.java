package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SCEditorRenderer extends SnipScopeRenderer {
    private SCEditorWindow scEditorWindow;
    private BufferedImage preview;
    private Rectangle lastPreview;

    public SCEditorRenderer(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(preview == null)
            preview = new BufferedImage(scEditorWindow.getImage().getWidth(), scEditorWindow.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);

        Graphics2D previewGraphics = (Graphics2D) preview.getGraphics();
        if(lastPreview != null) {
            previewGraphics.setRenderingHints(scEditorWindow.getQualityHints());
            previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            previewGraphics.fillRect(lastPreview.x, lastPreview.y, lastPreview.width, lastPreview.height);
            previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }
        lastPreview = scEditorWindow.getSelectedStamp().render(previewGraphics, scEditorWindow.getInputContainer(), false, false, -1);
        previewGraphics.dispose();

        g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
    }
}
