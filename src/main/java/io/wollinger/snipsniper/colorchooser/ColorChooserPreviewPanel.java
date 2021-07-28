package io.wollinger.snipsniper.colorchooser;

import io.wollinger.snipsniper.utils.SSColor;

import javax.swing.*;
import java.awt.*;

public class ColorChooserPreviewPanel extends JPanel {
    ColorChooserSingle panelSingle;
    ColorChooserGradient panelGradient;
    JTabbedPane tabPane;

    public ColorChooserPreviewPanel(ColorChooser colorChooser) {
        setPreferredSize(new Dimension(0, 256));
        setLayout(new GridLayout());
        tabPane = new JTabbedPane(JTabbedPane.TOP);

        panelSingle = new ColorChooserSingle(this);
        panelSingle.setColor(Color.RED);

        panelGradient = new ColorChooserGradient();
        panelGradient.setColor(SSColor.fromSaveString("#00D350___#003A19"));

        tabPane.addTab("Single color", panelSingle);
        tabPane.addTab("Gradient",  panelGradient);
        add(tabPane);
    }
}
