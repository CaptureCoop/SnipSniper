package io.wollinger.snipsniper.scviewer;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;
import io.wollinger.snipsniper.snipscope.SnipScopeRenderer;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SCViewerWindow extends SnipScopeWindow {
    private File currentFile;
    private ArrayList<String> files = new ArrayList<>();

    private final List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg");

    private boolean locked = false;

    public SCViewerWindow(File file) {
        currentFile = file;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        refreshTitle();
        setIconImage(Icons.icon_viewer);
        BufferedImage image;
        if(file != null) {
            refreshFolder();
            image = getImageFromFile(currentFile);
        } else {
            image = Utils.getDragPasteImage(Icons.icon_viewer, "Drop image here!");
        }

        SCViewerListener listener = new SCViewerListener(this);
        SnipScopeRenderer renderer = new SnipScopeRenderer(this);
        renderer.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    currentFile = (File) droppedFiles.get(0);
                    setImage(getImageFromFile(currentFile));
                    refreshFolder();
                    refreshTitle();
                    repaint();
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        init(image, renderer , listener);

        setVisible(true);
        setSizeAuto();
        setLocationAuto();
    }

    public void refreshTitle() {
        String title = "SnipSniper Viewer";
        if(currentFile != null)
            title += " (" + currentFile.getAbsolutePath() + ")";
        setTitle(title);
    }

    public void refreshFolder() {
        File path = new File(currentFile.getAbsolutePath().replace(currentFile.getName(), ""));
        ArrayList<File> folder = new ArrayList<>(Arrays.asList(path.listFiles()));
        files.clear();
        for(File cFile : folder) {
            if(extensions.contains(Utils.getFileExtension(cFile).toLowerCase()))
                files.add(cFile.getAbsolutePath());
        }
    }

    public void slideImage(int direction) {
        locked = true;
        int index = files.indexOf(currentFile.getAbsolutePath());
        if(direction == -1) {
            if(index > 0)
                index--;
            else
                index = files.size()-1;
        } else if(direction == 1) {
            if(index < files.size()-1)
                index++;
            else
                index = 0;
        }
        File newFile = new File(files.get(index));
        if(!currentFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            currentFile = newFile;
            setImage(getImageFromFile(currentFile));
            refreshTitle();
        }
        locked = false;
        resetZoom();
    }

    public void openEditor() {
        if(currentFile != null) {
            Config config = SCEditorWindow.getStandaloneEditorConfig();
            config.save();

            SCEditorWindow editor = new SCEditorWindow("EDIT", getImage(), (int) getLocation().getX(), (int) getLocation().getY(), "SnipSniper Editor", config, false, currentFile.getAbsolutePath(), false, true);
            editor.setSize(getSize());
            if (config.getBool("closeViewerOnOpenEditor"))
                dispose();
        }
    }

    public BufferedImage getImageFromFile(File file) {
        if(file.exists()) {
            if(!extensions.contains(Utils.getFileExtension(file).toLowerCase()))
                return null;

            BufferedImage image = null;
            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return image;
        }
        return null;
    }

    public boolean isLocked() {
        return locked;
    }

}
