package io.wollinger.snipsniper.editorwindow;

import javax.imageio.ImageIO;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class EditorDropTarget extends DropTarget {
    private final EditorWindow editorWindow;

    public EditorDropTarget(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            BufferedImage image = ImageIO.read((File) droppedFiles.get(0));
            editorWindow.initImage(image, ((File) droppedFiles.get(0)).getAbsolutePath());
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }

    }
}
