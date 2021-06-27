package io.wollinger.snipsniper.configwindow;

import javax.swing.*;
import java.awt.*;

public class StampJPanel extends JPanel {
    public StampJPanel(GridBagLayout gridLayout) {
        super(gridLayout);
    }

    private JComponent comp;
    private JFrame jpanel;

    public void addDrawComponent(JFrame panel, JComponent comp) {
        this.comp = comp;
        this.jpanel = panel;
    }

    public void paint(Graphics g) {
        super.paint(g);
        jpanel.revalidate();
        int y = (int) (comp.getLocationOnScreen().getY() - getLocationOnScreen().getY());

//        if(comp != null)
//        g.fillRect((int)comp.getLocation().getX(),y,64,64);
    }

}
