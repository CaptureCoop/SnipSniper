package org.snipsniper.scviewer;

import org.snipsniper.SnipSniper;
import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;
import org.snipsniper.snipscope.SnipScopeWindow;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.enums.ClockDirection;
import org.snipsniper.utils.FileUtils;
import org.snipsniper.ImageManager;
import org.snipsniper.utils.Utils;
import org.apache.commons.lang3.SystemUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SCViewerWindow extends SnipScopeWindow {
    private File currentFile;
    private final ArrayList<String> files = new ArrayList<>();
    private final Config config;

    private final List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg");

    private boolean locked = false;

    private JMenuItem saveItem;
    private BufferedImage defaultImage;

    public SCViewerWindow(File file, Config config, boolean isStandalone) {
        currentFile = file;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        refreshTitle();
        setIconImage(ImageManager.getImage("icons/viewer.png"));
        BufferedImage image;
        if(file != null) {
            refreshFolder();
            image = getImageFromFile(currentFile);
        } else {
            image = Utils.getDragPasteImage(ImageManager.getImage("icons/viewer.png"), "Drop image here!");
            defaultImage = image;
        }

        if(config == null) {
            this.config = new Config("viewer.cfg", "profile_defaults.cfg");
            this.config.save();
        } else {
            this.config = config;
        }

        setRequireMovementKeyForZoom(false);

        SCViewerListener listener = new SCViewerListener(this);
        SCViewerRenderer renderer = new SCViewerRenderer(this);
        renderer.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List droppedFiles = (List) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    setImage((File) droppedFiles.get(0));
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        init(image, renderer , listener);

        setVisible(true);
        setSizeAuto();
        setLocationAuto();
        if(this.config.getBool(ConfigHelper.PROFILE.openViewerInFullscreen))
            setExtendedState(getExtendedState() | MAXIMIZED_BOTH);

        if(SystemUtils.IS_OS_WINDOWS) {
            JMenuBar topBar = new JMenuBar();
            JMenuItem rotateCClockwise = new JMenuItem("↶");
            rotateCClockwise.addActionListener(e -> rotateImage(ClockDirection.COUNTERCLOCKWISE));
            topBar.add(rotateCClockwise);
            JMenuItem rotateClockwise = new JMenuItem("↷");
            rotateClockwise.addActionListener(e -> rotateImage(ClockDirection.CLOCKWISE));
            topBar.add(rotateClockwise);

            saveItem = new JMenuItem("Save");
            saveItem.addActionListener(e -> {
                try {
                    ImageIO.write(getImage(), "png", currentFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                saveItem.setEnabled(false);
            });
            topBar.add(saveItem);
            saveItem.setEnabled(false);
            setJMenuBar(topBar);
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(isStandalone)
                    SnipSniper.exit(false);
            }
        });
        setEnableInteraction(!isDefaultImage());
    }

    public void rotateImage(ClockDirection direction) {
        if(direction == ClockDirection.CLOCKWISE)
            setImage(Utils.rotateClockwise90(getImage()));
        else //TODO: Ugly hack, figure out better solution
            setImage(Utils.rotateClockwise90(Utils.rotateClockwise90(Utils.rotateClockwise90(getImage()))));
        if(currentFile != null)
            saveItem.setEnabled(true);
        repaint();
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
            if(extensions.contains(FileUtils.getFileExtension(cFile).toLowerCase()))
                files.add(cFile.getAbsolutePath());
        }
    }

    public void slideImage(int direction) {
        if(locked)
            return;

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
            setImage(newFile);
        }
        locked = false;
        resetZoom();
    }

    public void openEditor() {
        if(currentFile != null) {
            SCEditorWindow editor = new SCEditorWindow(getImage(), (int) getLocation().getX(), (int) getLocation().getY(), "SnipSniper Editor", config, false, currentFile.getAbsolutePath(), false, false);
            editor.setSize(getSize());
            if (config.getBool(ConfigHelper.PROFILE.closeViewerOnOpenEditor))
                dispose();
        }
    }

    public void setImage(File file) {
        BufferedImage newImage = Utils.imageToBufferedImage(new ImageIcon(file.getAbsolutePath()).getImage());
        super.setImage(newImage);
        currentFile = new File(file.getAbsolutePath());
        setEnableInteraction(!isDefaultImage());
        refreshTitle();
        refreshFolder();
        repaint();
    }

    public BufferedImage getImageFromFile(File file) {
        if(file.exists()) {
            if(!extensions.contains(FileUtils.getFileExtension(file).toLowerCase()))
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

    public boolean isDefaultImage() {
        return getImage() == defaultImage;
    }

}
