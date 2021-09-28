package org.snipsniper.utils.debug;

import org.json.JSONObject;
import org.snipsniper.ImageManager;
import org.snipsniper.LangManager;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.utils.Utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class LangDebugWindow extends JFrame {
    private JScrollPane scrollPane;

    public LangDebugWindow() {
        setTitle("Debug Language Window");
        setSize(512, 512);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(ImageManager.getImage("icons/config.png"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) screenSize.getWidth();
        int height = (int) screenSize.getHeight();
        setLocation(width / 2 - getWidth() / 2, height / 2 - getHeight() / 2);
        scrollPane = setup();
        add(scrollPane);
        resetScrollPane();
        setVisible(true);
    }

    private JScrollPane setup() {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        setupLabels("en", content, gbc);
        return ConfigWindow.generateScrollPane(content);
    }

    public void resetScrollPane() {
        Runnable doScroll = () -> scrollPane.getVerticalScrollBar().setValue(0);
        SwingUtilities.invokeLater(doScroll);
    }

    public void setupLabels(String language, JPanel content, GridBagConstraints gbc) {
        content.removeAll();
        gbc.gridx = 0;
        content.add(new JLabel("English", JLabel.CENTER), gbc);
        gbc.gridx = 1;
        content.add(Utils.getLanguageDropdown(language, args -> {
            setupLabels(args[0], content, gbc);
            resetScrollPane();
        }), gbc);
        gbc.fill = GridBagConstraints.BOTH;
        JSONObject enJSON = LangManager.getJSON("en").getJSONObject("strings");
        enJSON.keySet().forEach(keyStr -> {
            gbc.gridx = 0;
            gbc.insets.top = 5;
            gbc.insets.right = 5;
            content.add(createLabel(LangManager.getItem("en", keyStr ), false), gbc);
            gbc.gridx = 1;
            gbc.insets.right = 0;
            gbc.insets.left = 5;
            content.add(createLabel(LangManager.getItem(language, keyStr), true), gbc);
            gbc.insets.right = 0;
            gbc.insets.left = 0;
        });
        content.validate();
    }

    public JTextArea createLabel(String text, boolean editable) {
        JTextArea multi = new JTextArea(text);
        multi.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        multi.setWrapStyleWord(true);
        multi.setLineWrap(true);
        multi.setEditable(editable);
        return multi;
    }

}
