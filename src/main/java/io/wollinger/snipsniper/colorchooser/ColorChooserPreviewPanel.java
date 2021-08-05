package io.wollinger.snipsniper.colorchooser;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ColorChooserPreviewPanel extends JPanel {
    ColorChooserSingle panelSingle;
    ColorChooserGradient panelGradient;
    JTabbedPane tabPane;

    public ColorChooserPreviewPanel(ColorChooser colorChooser) {
        setPreferredSize(new Dimension(0, 256));
        setLayout(new GridLayout());
        tabPane = new JTabbedPane(JTabbedPane.TOP);

        panelSingle = new ColorChooserSingle(colorChooser);

        panelGradient = new ColorChooserGradient(colorChooser);

        colorChooser.getJcc().getSelectionModel().addChangeListener(e -> {
            if(tabPane.getSelectedIndex() == 0)
                colorChooser.getColor().setPrimaryColor(colorChooser.getJcc().getColor());
            else if(tabPane.getSelectedIndex() == 1)
                panelGradient.setColorAuto(colorChooser.getJcc().getColor());
        });

        tabPane.addTab("Single color", panelSingle);
        tabPane.addTab("Gradient",  panelGradient);

        if(colorChooser.getColor().isGradient())
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
