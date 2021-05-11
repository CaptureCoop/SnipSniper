package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SCEditorRenderer extends SnipScopeRenderer {
    private final SCEditorWindow scEditorWindow;
    private BufferedImage preview;

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
        previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        previewGraphics.fillRect(0,0, preview.getWidth(), preview.getHeight());
        previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        scEditorWindow.getSelectedStamp().render(previewGraphics, scEditorWindow.getInputContainer(), false, false, -1);
        previewGraphics.dispose();

        g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
    }
}
