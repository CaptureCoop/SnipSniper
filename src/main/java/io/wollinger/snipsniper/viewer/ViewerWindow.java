package io.wollinger.snipsniper.viewer;

import io.wollinger.snipsniper.utils.Utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ViewerWindow extends JFrame {

    private File currentFile;
    private ArrayList<File> folder;

    private BufferedImage image;

    private List<String> extensions = Arrays.asList(".png", ".jpg", ".jpeg");
    //TODO: add more extensions

    public ViewerWindow(File file) {
        currentFile = file;
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshTitle();
        setSize(512,512);
        add(new ViewerWindowRender(this));
        if(file != null) {
            refreshFolder();
            initImage(file);
        }
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
        folder = new ArrayList<>(Arrays.asList(path.listFiles()));
        ArrayList<File> filesToRemove = new ArrayList<>();
        for(File cFile : folder) {
            if(!extensions.contains(Utils.getFileExtension(cFile)))
                filesToRemove.add(cFile);
        }
        for(File cFile : filesToRemove)
            folder.remove(cFile);
    }

    public void initImage(File file) {
        if(file.exists()) {
            //TODO: Handle file not existing / not beeing image
            if(!extensions.contains(Utils.getFileExtension(file)))
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

}
