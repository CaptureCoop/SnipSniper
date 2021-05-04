package io.wollinger.snipsniper.viewer;

import javax.swing.*;
import java.awt.*;

public class ViewerWindowRender extends JPanel {
    private ViewerWindow viewerWindow;

    public ViewerWindowRender(ViewerWindow viewerWindow) {
        this.viewerWindow = viewerWindow;
        setDropTarget(new ViewerWindowDropTarget(viewerWindow));
    }

    public void paint(Graphics g) {
        g.drawImage(viewerWindow.getImage(), 0,0, this);
    }

}
