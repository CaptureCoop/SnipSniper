package io.wollinger.snipsniper.viewer;

import javax.swing.*;
import java.io.File;

public class ViewerWindow extends JFrame {

    private File currentFile;

    public ViewerWindow(File file) {
        currentFile = file;
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        refreshTitle();
        setSize(512,512);
        setVisible(true);
    }

    public void refreshTitle() {
        String title = "SnipSniper Viewer";
        if(currentFile != null)
            title += " (" + currentFile.getAbsolutePath() + ")";
        setTitle(title);
    }

}
