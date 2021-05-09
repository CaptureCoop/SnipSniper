package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;
import io.wollinger.snipsniper.utils.Vector2Int;

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

        //previewGraphics.fillRect(lastRectangle .x + pos.x, lastRectangle.y + pos.y, lastPreview.width, lastPreview.height);
        //g.fillRect(lastRectangle.x - scEditorWindow.getInputContainer().getMouseX(), lastRectangle.y + scEditorWindow.getInputContainer().getMouseY(), 50,50);
        int x = 0;
        int y = 0;
        int w = 100;
        int h = 100;
        int mouseX = scEditorWindow.getInputContainer().getMouseX();
        int mouseY = scEditorWindow.getInputContainer().getMouseY();

        Vector2Int pos1 = scEditorWindow.getPointOnImage(new Point(mouseX, mouseY));
        Vector2Int pos2 = scEditorWindow.getPointOnImage(new Point(mouseX + lastPreview.width, mouseY + lastPreview.height));

        int dx = pos2.x - pos1.x;
        int dy = pos2.y - pos1.y;
        System.out.println(pos1 + " " + pos2);


        //g.drawRect(x + mouseX,y + mouseY,dx,dy);
        //g.drawImage(preview, x, y, w, h, lastPreview.x, lastPreview.y, lastPreview.width, lastPreview.height, this);
        g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
        //g.drawRect(lastPreview.x, lastPreview.y, lastPreview.width, lastPreview.height);
    }
}
