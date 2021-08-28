package org.snipsniper.utils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FolderPreview extends JFrame {
    private String text = "";
    private FolderPreviewRenderer renderer;
    private JTextField input;

    public FolderPreview(String title, Function onClose) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(title);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if(onClose != null)
                    onClose.run();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Dimension newSize = new Dimension(getRootPane().getWidth(), getContentPane().getHeight() - input.getHeight());
                renderer.setPreferredSize(newSize);
                renderer.setMinimumSize(newSize);
                renderer.revalidate();
            }
        });
        setSize(256, 256);
        setupUI();
        setVisible(true);
        requestFocus();
        pack();
    }

    private void setupUI() {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        renderer = new FolderPreviewRenderer(this, 512, 256);
        content.add(renderer, gbc);
        gbc.gridy = 1;
        input = new JTextField("/");
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                text = input.getText();
                renderer.refresh();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                text = input.getText();
                renderer.refresh();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                text = input.getText();
                renderer.refresh();
            }
        });
        content.add(input, gbc);

        add(content);
    }

    public String getText() {
        return text;
    }

}
