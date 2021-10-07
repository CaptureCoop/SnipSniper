package org.snipsniper.colorchooser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorChooserPreviewPanel extends JPanel {
    ColorChooserSingle panelSingle;
    ColorChooserGradient panelGradient;
    JTabbedPane tabPane;

    public ColorChooserPreviewPanel(ColorChooser colorChooser, boolean useGradient, BufferedImage previewBackground) {
        setPreferredSize(new Dimension(0, 256));
        setLayout(new GridLayout());
        tabPane = new JTabbedPane(JTabbedPane.TOP);

        panelSingle = new ColorChooserSingle(colorChooser, previewBackground);

        if(useGradient)
            panelGradient = new ColorChooserGradient(colorChooser, previewBackground);

        colorChooser.getJcc().getSelectionModel().addChangeListener(e -> {
            if(tabPane.getSelectedIndex() == 0)
                colorChooser.getColor().setPrimaryColor(colorChooser.getJcc().getColor());
            else if(tabPane.getSelectedIndex() == 1)
                panelGradient.setColorAuto(colorChooser.getJcc().getColor());
        });

        tabPane.addTab("Single color", panelSingle);
        if(useGradient)
            tabPane.addTab("Gradient",  panelGradient);

        if(colorChooser.getColor().isGradient() && useGradient)
            tabPane.setSelectedIndex(1);

        tabPane.addChangeListener(e -> {
            switch(tabPane.getSelectedIndex()) {
                case 0: colorChooser.getColor().setIsGradient(false); break;
                case 1: colorChooser.getColor().setIsGradient(true); break;
            }
        });
        add(tabPane);
    }
}
