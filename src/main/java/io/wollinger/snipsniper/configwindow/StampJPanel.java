package io.wollinger.snipsniper.configwindow;

import javax.swing.*;
import java.awt.*;

public class StampJPanel extends JPanel {

    public void paint(Graphics g) {
        super.paint(g);
        int width = 64;
        int height = 64;
        g.drawRect(0,0,getWidth()-1,getHeight()-1);
        g.setColor(Color.BLUE);
        g.fillRect(getWidth()/2 - width/2, getHeight()/2 - height/2, width, height);
    }

}
