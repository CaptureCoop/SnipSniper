package io.wollinger.snipsniper.colorchooser;

import javax.swing.*;
import java.awt.*;

public class ColorChooserPreviewPanel extends JPanel {
    public ColorChooserPreviewPanel(ColorChooser colorChooser) {
        setPreferredSize(new Dimension(colorChooser.getWidth(), 256));
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP);
        ColorChooserSingle panelSingle = new ColorChooserSingle(colorChooser.getWidth(), 256);
        panelSingle.setColor(Color.RED);
        tabPane.addTab("Single color", panelSingle);
        tabPane.addTab("Gradient",  new JPanel());
        add(tabPane);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //g.fillRect(0,0, getHeight(), getHeight());
    }
}
