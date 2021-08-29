package org.snipsniper.utils;

import org.snipsniper.LangManager;
import org.snipsniper.config.ConfigHelper;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;

public class FolderPreview extends JFrame {
    private String text = "";
    private FolderPreviewRenderer renderer;
    private JTextField input;
    JButton saveButton = new JButton(LangManager.getItem("config_label_save"));

    private Function onSave;
    private Function onClose;

    public FolderPreview(String title, String content) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setTitle(title);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                if(onClose != null) onClose.run();
            }
        });
        text = content;
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Dimension newSize = new Dimension(getRootPane().getWidth(), getContentPane().getHeight() - input.getHeight() - saveButton.getHeight());
                renderer.setPreferredSize(newSize);
                renderer.setMinimumSize(newSize);
                renderer.revalidate();
            }
        });
        setSize(256, 256);
        setIconImage(Icons.getImage("icons/folder.png"));
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
        input = new JTextField(text);
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
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        saveButton.addActionListener(e -> {
            if(onSave != null) onSave.run();
            if(onClose != null) onClose.run();
            dispose();
        });
        content.add(saveButton, gbc);

        add(content);
    }

    public void setOnClose(Function function) {
        onClose = function;
    }

    public void setOnSave(Function function) {
        onSave = function;
    }

    public String getText() {
        return text;
    }

}
