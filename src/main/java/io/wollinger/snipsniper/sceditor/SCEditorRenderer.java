package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SCEditorRenderer extends SnipScopeRenderer {
    private final SCEditorWindow scEditorWindow;
    private BufferedImage preview;

    public SCEditorRenderer(SCEditorWindow snipScopeWindow) {
        super(snipScopeWindow);
        scEditorWindow = snipScopeWindow;
        setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    BufferedImage image = ImageIO.read((File) droppedFiles.get(0));
                    scEditorWindow.setSaveLocation(((File) droppedFiles.get(0)).getAbsolutePath());
                    scEditorWindow.setInClipboard(false);
                    scEditorWindow.refreshTitle();
                    scEditorWindow.setImage(image, true, true);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public void resetPreview() {
        preview = new BufferedImage(scEditorWindow.getImage().getWidth(), scEditorWindow.getImage().getHeight(), BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if(preview == null)
            resetPreview();

        Graphics2D previewGraphics = (Graphics2D) preview.getGraphics();
        previewGraphics.setRenderingHints(scEditorWindow.getQualityHints()); //TODO: Make this toggleable per stamp, for example not for rect stamp, but do it for counter stamp ^^ (Also add it to render function of each stamp..)
        previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
        previewGraphics.fillRect(0,0, preview.getWidth(), preview.getHeight());
        previewGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
        scEditorWindow.getSelectedStamp().render(previewGraphics, scEditorWindow.getInputContainer(), scEditorWindow.getPointOnImage(new Point(scEditorWindow.getInputContainer().getMouseX(), scEditorWindow.getInputContainer().getMouseY())), scEditorWindow.getDifferenceFromImage(), false, false, -1);
        previewGraphics.dispose();

        g.drawImage(preview, lastRectangle.x, lastRectangle.y, lastRectangle.width, lastRectangle.height, this);
        repaint(); //TODO: Add something like a "gameloop" to do this, this is just a hack
    }
}
