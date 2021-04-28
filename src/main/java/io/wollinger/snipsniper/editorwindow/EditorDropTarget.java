package io.wollinger.snipsniper.editorwindow;

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

public class EditorDropTarget extends DropTarget {
    private EditorWindow editorWindow;
    private EditorWindowRender render;

    public EditorDropTarget(EditorWindow editorWindow, EditorWindowRender render) {
        this.editorWindow = editorWindow;
        this.render = render;
    }

    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            editorWindow.setImage(ImageIO.read((File) droppedFiles.get(0)));
            if(!editorWindow.isStarted())
                editorWindow.start();
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }

    }
}
