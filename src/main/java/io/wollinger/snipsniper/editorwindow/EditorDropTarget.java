package io.wollinger.snipsniper.editorwindow;

import io.wollinger.snipsniper.utils.Utils;

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
    private final EditorWindow editorWindow;

    public EditorDropTarget(EditorWindow editorWindow) {
        this.editorWindow = editorWindow;
    }

    public synchronized void drop(DropTargetDropEvent evt) {
        try {
            evt.acceptDrop(DnDConstants.ACTION_COPY);
            List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            GraphicsDevice device = editorWindow.getGraphicsConfiguration().getDevice();
            BufferedImage image = ImageIO.read((File) droppedFiles.get(0));

            int monitorWidth = device.getDisplayMode().getWidth()-100;
            int monitorHeight = device.getDisplayMode().getHeight()-100;

            if(image.getWidth() >= monitorWidth || image.getHeight() > monitorHeight) {
                Dimension newDimension = Utils.getScaledDimension(image, new Dimension(monitorWidth, monitorHeight));
                image = Utils.imageToBufferedImage(image.getScaledInstance((int)newDimension.getWidth(), (int)newDimension.getHeight(), 5));
            }

            Rectangle rect = editorWindow.getGraphicsConfiguration().getBounds();
            editorWindow.setLocation((int)(rect.getX() + (rect.getWidth()/2) - (image.getWidth()/2)), (int)(rect.getY() + (rect.getHeight()/2) - image.getHeight()/2));

            editorWindow.setImage(image, true);
            if(!editorWindow.isStarted())
                editorWindow.start();
        } catch (UnsupportedFlavorException | IOException e) {
            e.printStackTrace();
        }

    }
}
