package org.snipsniper.configwindow.textpreviewwindow;

import org.snipsniper.LangManager;
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
import java.awt.image.BufferedImage;

public class TextPreviewWindow extends JFrame implements IClosable {
    private String text;
    private JTextField input;
    private final JPanel renderer;
    private final JButton saveButton = new JButton(LangManager.getItem("config_label_save"));
    private final JLabel explanationLabel = new JLabel("%hour%, %minute%, %second%, %day%, %month%, %year%, %random%");
    private IFunction onSave;

    public TextPreviewWindow(String title, String text, JPanel renderPanel, BufferedImage icon, JFrame parent, String explanation) {
        this.text = text;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(title);
        this.explanationLabel.setText(explanation);
        renderer = renderPanel;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent componentEvent) {
                Dimension newSize = new Dimension(getRootPane().getWidth(), getContentPane().getHeight() - input.getHeight() - saveButton.getHeight() - explanationLabel.getHeight());
                renderer.setPreferredSize(newSize);
                renderer.setMinimumSize(newSize);
                renderer.revalidate();
            }
        });
        setSize(256, 256);
        setIconImage(icon);
        setupUI();
        setVisible(true);
        requestFocus();
        pack();
        int x = (int) (parent.getLocation().getX() + parent.getWidth() / 2) - getWidth() / 2;
        int y = (int) (parent.getLocation().getY() + parent.getHeight() / 2) - getHeight() / 2;
        setLocation(x, y);
    }

    private void setupUI() {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridy = 0;
        content.add(renderer, gbc);
        gbc.gridy = 1;
        input = new JTextField(text);
        input.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent documentEvent) {
                update();
            }

            @Override
            public void removeUpdate(DocumentEvent documentEvent) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent documentEvent) {
                update();
            }

            public void update() {
                text = input.getText();
                renderer.repaint();
            }
        });
        content.add(input, gbc);
        gbc.gridy = 2;
        content.add(explanationLabel, gbc);
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

    public void setOnSave(IFunction onSave) {
        this.onSave = onSave;
    }

    @Override
    public void close() {
        if(onSave != null)
            onSave.run();
        dispose();
    }
}
