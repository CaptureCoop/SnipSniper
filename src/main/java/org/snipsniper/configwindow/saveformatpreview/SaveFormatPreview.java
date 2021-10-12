package org.snipsniper.configwindow.saveformatpreview;

import org.snipsniper.ImageManager;
import org.snipsniper.LangManager;
import org.snipsniper.configwindow.folderpreview.FolderPreviewRenderer;
import org.snipsniper.utils.IClosable;
import org.snipsniper.utils.IFunction;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SaveFormatPreview extends JFrame implements IClosable{
    private String text;
    private SaveFormatPreviewRenderer renderer;
    private JTextField input;
    private final JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
    private final JLabel explanation = new JLabel("%hour%, %minute%, %second%, %day%, %month%, %year%, %random%");
    private IFunction onSave;

    public SaveFormatPreview(String text) {
        this.text = text;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle("Save format");
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Dimension newSize = new Dimension(getRootPane().getWidth(), getContentPane().getHeight() - input.getHeight() - saveButton.getHeight() - explanation.getHeight());
                renderer.setPreferredSize(newSize);
                renderer.setMinimumSize(newSize);
                renderer.revalidate();
            }
        });
        setSize(256, 256);
        setIconImage(ImageManager.getImage("icons/folder.png"));
        setupUI();
        setVisible(true);
        requestFocus();
        pack();
    }

    public void setOnSave(IFunction function) {
        onSave = function;
    }

    private void setupUI() {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        renderer = new SaveFormatPreviewRenderer(this, 512, 256);
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
        content.add(explanation, gbc);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.VERTICAL;
        saveButton.addActionListener(e -> {
            if(onSave != null) onSave.run();
            dispose();
        });
        content.add(saveButton, gbc);

        add(content);
    }

    public String getText() {
        return text;
    }

    @Override
    public void close() {
        dispose();
    }
}
