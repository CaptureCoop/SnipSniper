package io.wollinger.snipsniper.viewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ViewerWindowDropTarget extends DropTarget  {
    private ViewerWindow viewerWindow;

    public ViewerWindowDropTarget (ViewerWindow viewerWindow) {
        this.viewerWindow = viewerWindow;
    }

    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            viewerWindow.setImage((File) droppedFiles.get(0));
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }
    }

}
