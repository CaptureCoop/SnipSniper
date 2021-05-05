package io.wollinger.snipsniper.viewer;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.editorwindow.EditorWindow;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ViewerWindow extends JFrame {

    private File currentFile;
    private ArrayList<String> files = new ArrayList<>();

    private BufferedImage image;

    private List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg");

    private boolean locked = false;

    public ViewerWindow(File file) {
        currentFile = file;
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshTitle();
        setSize(512,512);
        setIconImage(Icons.icon_viewer);
        add(new ViewerWindowRender(this));
        addKeyListener(new ViewerWindowListener(this));
        if(file != null) {
            refreshFolder();
            initImage(currentFile);
        } else {
            //TODO: Make this translation
            image = Utils.getDragPasteImage(Icons.icon_viewer, "Drop image here!");
        }

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((int)(screenSize.getWidth()/2 - getWidth()/2), (int)(screenSize.getHeight()/2 - getHeight()/2));

        setVisible(true);
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
            initImage(currentFile);
            refreshTitle();
        }
        locked = false;
    }

    public void openEditor() {
        Config config = EditorWindow.getStandaloneEditorConfig();
        config.save();
        new EditorWindow("EDIT", image, (int)getLocation().getX(), (int)getLocation().getY(), "SnipSniper Editor", config, false, currentFile.getAbsolutePath(), false, true);
        if(config.getBool("closeViewerOnOpenEditor"))
            dispose();
    }

    public void initImage(File file) {
        if(file.exists()) {
            if(!extensions.contains(Utils.getFileExtension(file).toLowerCase()))
                return;

            try {
                image = ImageIO.read(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

            GraphicsDevice device = getGraphicsConfiguration().getDevice();
            int monitorWidth = device.getDisplayMode().getWidth()-100;
            int monitorHeight = device.getDisplayMode().getHeight()-100;

            if(image.getWidth() >= monitorWidth || image.getHeight() > monitorHeight) {
                Dimension newDimension = Utils.getScaledDimension(image, new Dimension(monitorWidth, monitorHeight));
                image = Utils.imageToBufferedImage(image.getScaledInstance((int)newDimension.getWidth(), (int)newDimension.getHeight(), 5));
            }

            Insets insets = getInsets();
            setSize(insets.left + insets.right + image.getWidth(), insets.bottom + insets.top + image.getHeight());
        }
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(File file) {
        currentFile = file;
        initImage(file);
        refreshFolder();
        refreshTitle();
    }

    public boolean isLocked() {
        return locked;
    }

}
