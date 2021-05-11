package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SCEditorRenderer extends SnipScopeRenderer {
    private final SCEditorWindow scEditorWindow;
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
        previewGraphics.setRenderingHints(scEditorWindow.getQualityHints()); //TODO: Make this toggleable per stamp, for example not for rect stamp, but do it for counter stamp ^^ (Also add it to render function of each stamp..)
        if(lastPreview != null) {
            previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
            final int safeguard = 50;
            previewGraphics.fillRect(lastPreview.x-safeguard, lastPreview.y-safeguard, lastPreview.width+safeguard*2, lastPreview.height+safeguard*2);
            previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        }
        lastPreview = scEditorWindow.getSelectedStamp().render(previewGraphics, scEditorWindow.getInputContainer(), false, false, -1);
        previewGraphics.dispose();

        g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
    }
}
