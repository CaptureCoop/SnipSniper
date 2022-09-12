package net.snipsniper.utils.debug;

import net.snipsniper.ImageManager;
import net.snipsniper.LangManager;
import net.snipsniper.SnipSniper;
import net.snipsniper.configwindow.ConfigWindow;
import net.snipsniper.utils.FileUtils;
import net.snipsniper.utils.Utils;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class LangDebugWindow extends JFrame {
    private final JScrollPane scrollPane;
    private JSONObject currentEdit;
    private final ArrayList<JTextArea> textAreaList = new ArrayList<>();
    private final HashMap<JTextArea, String> keyMap = new HashMap<>();
    private String lastLanguage;

    public LangDebugWindow() {
        setTitle("Debug Language Window");
        setSize(512, 512);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImage(ImageManager.getImage("icons/config.png"));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        setLocation(width / 2 - getWidth() / 2, height / 2 - getHeight() / 2);
        scrollPane = setup();
        add(scrollPane);
        resetScrollPane();
        setVisible(true);
    }

    private JScrollPane setup() {
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        setupLabels("en", content);
        return ConfigWindow.generateScrollPane(content);
    }

    public void resetScrollPane() {
        Runnable doScroll = () -> scrollPane.getVerticalScrollBar().setValue(0);
        SwingUtilities.invokeLater(doScroll);
    }

    public void setupLabels(String language, JPanel content) {
        GridBagConstraints gbc = new GridBagConstraints();
        content.removeAll();
        gbc.gridx = 0;
        content.add(new JLabel("English", JLabel.CENTER), gbc);
        gbc.gridx = 1;
        content.add(Utils.getLanguageDropdown(language, args -> {
            setupLabels(args[0], content);
            resetScrollPane();
            lastLanguage = args[0];
        }), gbc);
        lastLanguage = "en";
        gbc.gridx = 0;
        content.add(getEmptyPanel(), gbc);
        gbc.gridx = 1;
        content.add(getEmptyPanel(), gbc);
        gbc.fill = GridBagConstraints.BOTH;
        JSONObject enJSON = LangManager.Companion.getJSON("en").getJSONObject("strings");
        currentEdit = LangManager.Companion.getJSON(language);
        enJSON.keySet().forEach(keyStr -> {
            gbc.gridx = 0;
            gbc.insets.top = 5;
            gbc.insets.right = 5;
            content.add(createLabel(LangManager.Companion.getItem("en", keyStr ), false, null), gbc);
            gbc.gridx = 1;
            gbc.insets.right = 0;
            gbc.insets.left = 5;
            content.add(createLabel(LangManager.Companion.getItem(language, keyStr), true, keyStr), gbc);
            gbc.insets.right = 0;
            gbc.insets.left = 0;
        });
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            for(JTextArea textArea : textAreaList) {
                String key = keyMap.get(textArea);
                JSONObject toEdit = currentEdit.getJSONObject("strings");
                if(toEdit.has(key))
                    toEdit.remove(key);
                toEdit.put(key, textArea.getText());
            }
            FileUtils.printFile(SnipSniper.Companion.getMainFolder() + "//" + lastLanguage + ".json", currentEdit.toString());
            FileUtils.openFolder(SnipSniper.Companion.getMainFolder());
            SnipSniper.Companion.resetProfiles();
        });
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        content.add(saveButton, gbc);
        content.validate();
    }

    public JPanel getEmptyPanel() {
        JPanel panel = new JPanel();
        Dimension dim = new Dimension(220, 10);
        panel.setPreferredSize(dim);
        return panel;
    }

    public JTextArea createLabel(String text, boolean editable, String key) {
        JTextArea multi = new JTextArea(text);
        if(key != null) {
            keyMap.put(multi, key);
            textAreaList.add(multi);
        }
        multi.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLACK));
        multi.setWrapStyleWord(true);
        multi.setLineWrap(true);
        multi.setEditable(editable);
        return multi;
    }

}
