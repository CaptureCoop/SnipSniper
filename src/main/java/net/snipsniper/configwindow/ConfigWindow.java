package net.snipsniper.configwindow;

import org.capturecoop.cccolorutils.CCColor;
import org.capturecoop.cccolorutils.chooser.CCColorChooser;
import org.capturecoop.ccutils.utils.CCMathUtils;
import net.snipsniper.ImageManager;
import net.snipsniper.LangManager;
import org.capturecoop.cclogger.CCLogger;
import net.snipsniper.SnipSniper;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.configwindow.tabs.*;
import net.snipsniper.systray.Sniper;
import net.snipsniper.utils.*;
import net.snipsniper.utils.enums.ConfigSaveButtonState;
import org.capturecoop.cclogger.CCLogLevel;
import org.capturecoop.ccutils.utils.CCIClosable;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class ConfigWindow extends JFrame implements CCIClosable {
    private final ArrayList<CustomWindowListener> listeners = new ArrayList<>();
    private final ArrayList<File> configFiles = new ArrayList<>();
    private Config lastSelectedConfig;

    public enum PAGE {generalPanel, editorPanel, viewerPanel, globalPanel}

    private GeneralTab generalTab;
    private EditorTab editorTab;
    private ViewerTab viewerTab;
    private GlobalTab globalTab;
    private final ITab[] tabs = new ITab[4];
    private int activeTabIndex;
    private int activeDropdownIndex = 0;

    private final ArrayList<CCIClosable> cWindows = new ArrayList<>();

    public ConfigWindow(Config config, PAGE page) {
        CCLogger.log("Creating config window");

        setSize(512, 512);
        setTitle(LangManager.getItem("config_label_config"));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setIconImage(ImageManager.getImage("icons/config.png"));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if(tabs[activeTabIndex].isDirty()) {
                    if(showDirtyWarning() == JOptionPane.NO_OPTION)
                        return;
                }
                close();
            }
        });

        refreshConfigFiles();

        setup(config, page);
        setVisible(true);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screenSize.width / 2 - getWidth() / 2, screenSize.height / 2 - getHeight() / 2);
        setSize((int)(generalTab.getWidth()*1.25F), getHeight());
    }

    public void refreshConfigFiles() {
        configFiles.clear();
        File cfgFolder = new File(SnipSniper.getConfigFolder());
        File[] files = cfgFolder.listFiles();
        if(files != null) {
            for (File file : files) {
                if (FileUtils.getFileExtension(file).equals(Config.DOT_EXTENSION))
                    configFiles.add(file);
            }
        }
    }

    public void setup(Config config, PAGE page) {
        JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);

        final int iconSize = 16;
        int index = 0;
        activeTabIndex = index;

        lastSelectedConfig = config;

        generalTab = new GeneralTab(this);
        generalTab.setup(config);
        tabPane.addTab("SnipSniper",  generateScrollPane(generalTab));
        tabPane.setIconAt(index, new ImageIcon(ImageManager.getImage("icons/snipsniper.png").getScaledInstance(iconSize, iconSize, 0)));
        tabs[index] = generalTab;
        index++;

        editorTab = new EditorTab(this);
        editorTab.setup(config);
        tabPane.addTab("Editor",  generateScrollPane(editorTab));
        tabPane.setIconAt(index, new ImageIcon(ImageManager.getImage("icons/editor.png").getScaledInstance(iconSize,iconSize,0)));
        tabs[index] = editorTab;
        if(page == PAGE.editorPanel)
            activeTabIndex = index;
        index++;

        viewerTab = new ViewerTab(this);
        viewerTab.setup(config);
        tabPane.addTab("Viewer", generateScrollPane(viewerTab));
        tabPane.setIconAt(index, new ImageIcon(ImageManager.getImage("icons/viewer.png").getScaledInstance(iconSize,iconSize,0)));
        tabs[index] = viewerTab;
        if(page == PAGE.viewerPanel)
            activeTabIndex = index;
        index++;

        globalTab = new GlobalTab(this);
        globalTab.setup(config);
        tabPane.addTab("Global", generateScrollPane(globalTab));
        tabPane.setIconAt(index, new ImageIcon(ImageManager.getImage("icons/config.png").getScaledInstance(iconSize, iconSize, 0)));
        tabs[index] = globalTab;
        if(page == PAGE.globalPanel)
            activeTabIndex = index;

        tabPane.addChangeListener(e -> {
            if(tabs[activeTabIndex].isDirty()) {
                tabs[activeTabIndex].setDirty(false);
                int requestedIndex = tabPane.getSelectedIndex();
                tabPane.setSelectedIndex(activeTabIndex);
                if(showDirtyWarning() == JOptionPane.YES_OPTION) {
                    setupPaneDynamic(config, tabs[activeTabIndex].getPage());
                    setupPaneDynamic(config, tabs[requestedIndex].getPage());
                    tabPane.setSelectedIndex(requestedIndex);
                    return;
                }
                tabs[activeTabIndex].setDirty(true);
            }
            activeTabIndex = tabPane.getSelectedIndex();
        });

        tabPane.setSelectedIndex(activeTabIndex);

        add(tabPane);
    }

    public static JScrollPane generateScrollPane(JComponent component) {
        JScrollPane scrollPane = new JScrollPane(component);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    public void msgError(String msg) {
        Utils.showPopup(this, msg, LangManager.getItem("config_sanitation_error"), JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, ImageManager.getImage("icons/redx.png"), true);
    }

    public void setupPaneDynamic(Config config, PAGE page) {
        switch(page) {
            case generalPanel: generalTab.setup(config);
            case editorPanel: editorTab.setup(config);
            case viewerPanel: viewerTab.setup(config);
            case globalPanel: globalTab.setup(config);
        }
    }

    public JComboBox<DropdownItem> setupProfileDropdown(JPanel panelToAdd, JPanel parentPanel, Config configOriginal, Config config, PAGE page, String... blacklist) {
        //Returns the dropdown, however dont add it manually
        //TODO: Refresh other dropdowns when creating new profile?
        ArrayList<DropdownItem> profiles = new ArrayList<>();
        for(File file : configFiles) {
            if(file.getName().contains("viewer")) {
                boolean add = true;
                for(String str : blacklist)
                    if (str.contains("viewer")) {
                        add = false;
                        break;
                    }
                if(add)
                    profiles.add(0, new DropdownItem("Standalone Viewer", file.getName(), ImageManager.getImage("icons/viewer.png")));
            } else if(file.getName().contains("editor")) {
                boolean add = true;
                for(String str : blacklist)
                    if (str.contains("editor")) {
                        add = false;
                        break;
                    }
                if(add)
                    profiles.add(0, new DropdownItem("Standalone Editor", file.getName(), ImageManager.getImage("icons/editor.png")));
            } else if(file.getName().contains("profile")) {
                int nr = getIDFromFilename(file.getName());
                Image img = ImageUtils.getIconDynamically(new Config(file.getName(), "profile_defaults.cfg"));
                if(img == null)
                    img = ImageUtils.getDefaultIcon(nr);
                String title = "Profile " + nr;
                Sniper sniper = SnipSniper.getProfile(nr);
                if(sniper != null)
                    title = sniper.getTitle();
                profiles.add(new DropdownItem(title, file.getName(), img));
            }
        }

        if(configOriginal == null)
            profiles.add(0, new DropdownItem("Select a profile", "select_profile"));

        DropdownItem[] items = new DropdownItem[profiles.size()];
        for(int i = 0; i < profiles.size(); i++)
            items[i] = profiles.get(i);

        JComboBox<DropdownItem> dropdown = new JComboBox<>(items);
        dropdown.setRenderer(new DropdownItemRenderer(items));
        if(configOriginal == null)
            dropdown.setSelectedIndex(0);
        else
            activeDropdownIndex = DropdownItem.setSelected(dropdown, config.getFilename());

        final ItemListener[] dropdownListener = {null};
        dropdownListener[0] = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int requestedItem = dropdown.getSelectedIndex();
                if(tabs[activeTabIndex].isDirty()) {
                    dropdown.removeItemListener(dropdownListener[0]);
                    dropdown.setSelectedIndex(activeDropdownIndex);
                    dropdown.addItemListener(dropdownListener[0]);
                    if(showDirtyWarning() == JOptionPane.NO_OPTION)
                        return;
                    tabs[activeTabIndex].setDirty(false);
                    dropdown.setSelectedIndex(requestedItem);
                }
                parentPanel.removeAll();
                Config newConfig = new Config(((DropdownItem)e.getItem()).getID(), "profile_defaults.cfg");
                setupPaneDynamic(newConfig, page);
                lastSelectedConfig = newConfig;
                activeDropdownIndex = dropdown.getSelectedIndex();
            }
        };
        dropdown.addItemListener(dropdownListener[0]);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1F;
        panelToAdd.add(dropdown, gbc);
        gbc.gridx = 2;
        JPanel profilePlusMinus = new JPanel(new GridLayout(0, 2));
        JButton profileAddButton = new JButton("+");
        if(SnipSniper.getProfileCount() == SnipSniper.getProfileCountMax())
            profileAddButton.setEnabled(false);
        profileAddButton.addActionListener(actionEvent -> {
            if(tabs[activeTabIndex].isDirty()) {
                int result = showDirtyWarning();
                if(result == JOptionPane.NO_OPTION) {
                    return;
                }
            }
            for(int i = 0; i < SnipSniper.getProfileCountMax(); i++) {
                if(SnipSniper.getProfile(i) == null) {
                    SnipSniper.setProfile(i, new Sniper(i));
                    Config newProfileConfig = SnipSniper.getProfile(i).getConfig();
                    newProfileConfig.save();

                    refreshConfigFiles();
                    parentPanel.removeAll();

                    generalTab.setup(newProfileConfig);
                    editorTab.setup(newProfileConfig);
                    viewerTab.setup(newProfileConfig);

                    lastSelectedConfig = newProfileConfig;

                    break;
                }
            }
        });
        profilePlusMinus.add(profileAddButton);
        JButton profileRemoveButton = new JButton("-");
        DropdownItem selectedItem = (DropdownItem) dropdown.getSelectedItem();
        if(selectedItem != null) {
            if (selectedItem.getID().contains("profile0") || selectedItem.getID().contains("editor"))
                profileRemoveButton.setEnabled(false);
        }
        profileRemoveButton.addActionListener(actionEvent -> {
            //No dirty check needs to be performed, we are deleting it anyways
            DropdownItem item = (DropdownItem) dropdown.getSelectedItem();
            if(!item.getID().contains("profile0") || !item.getID().contains("editor")) {
                config.deleteFile();
                SnipSniper.resetProfiles();
                refreshConfigFiles();
                parentPanel.removeAll();
                int newIndex = dropdown.getSelectedIndex() - 1;
                if(newIndex < 0)
                    newIndex = dropdown.getSelectedIndex() + 1;
                Config newConfig = new Config(dropdown.getItemAt(newIndex).getID(), "profile_defaults.cfg");

                generalTab.setup(newConfig);
                editorTab.setup(newConfig);
                viewerTab.setup(newConfig);

                lastSelectedConfig = newConfig;
            }
        });
        profilePlusMinus.add(profileRemoveButton);
        panelToAdd.add(profilePlusMinus, gbc);
        return dropdown;
    }

    //Returns function you can run to update the state
    public Function setupSaveButtons(JPanel panel, ITab tab, GridBagConstraints gbc, Config config, Config configOriginal, IFunction beforeSave, boolean reloadOtherDropdowns) {
        final boolean[] allowSaving = {true};
        final boolean[] isDirty = {false};
        JButton save = new JButton(LangManager.getItem("config_label_save"));
        save.addActionListener(e -> {
            if(allowSaving[0] && configOriginal != null) {
                if(beforeSave != null)
                    beforeSave.run();
                configOriginal.loadFromConfig(config);
                configOriginal.save();
                for(CustomWindowListener listener : listeners)
                    listener.windowClosed();

                SnipSniper.resetProfiles();
                if(reloadOtherDropdowns) {
                    generalTab.setup(configOriginal);
                    editorTab.setup(configOriginal);
                    viewerTab.setup(configOriginal);
                }
            }
        });

        JButton close = new JButton(LangManager.getItem("config_label_close"));
        close.addActionListener(e -> {
            if(isDirty[0]) {
                if (showDirtyWarning() == JOptionPane.NO_OPTION)
                    return;
            }
            close();
        });
        Function setState = new Function() {
            @Override
            public boolean run(ConfigSaveButtonState state) {
                if(configOriginal == null) return false;
                switch (state) {
                    case UPDATE_CLEAN_STATE: isDirty[0] = !config.equals(configOriginal); tab.setDirty(isDirty[0]); break;
                    case YES_SAVE: allowSaving[0] = true; break;
                    case NO_SAVE: allowSaving[0] = false; break;
                }
                if(isDirty[0])
                    close.setText(LangManager.getItem("config_label_cancel"));
                else
                    close.setText(LangManager.getItem("config_label_close"));

                return true;
            }
        };
        gbc.insets.top = 20;
        gbc.gridx = 0;
        panel.add(save, gbc);
        gbc.gridx = 1;
        panel.add(close, gbc);
        return setState;
    }

    private int showDirtyWarning() {
        return Utils.showPopup(this, "Unsaved changes, are you sure you want to cancel?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ImageManager.getImage("icons/redx.png"), true);
    }

    public int getIDFromFilename(String name) {
        String idString = name.replaceAll(Config.DOT_EXTENSION, "").replace("profile", "");
        if(CCMathUtils.isInteger(idString)) {
            return Integer.parseInt(idString);
        }
        CCLogger.log("Issue parsing Filename to id: " + name, CCLogLevel.ERROR);
        return -1;
    }

    public GradientJButton setupColorButton(String title, Config config, ConfigHelper.PROFILE configKey, ChangeListener whenChange) {
        CCColor startColorPBR = CCColor.fromSaveString(config.getString(configKey));
        GradientJButton colorButton = new GradientJButton(title, startColorPBR);
        startColorPBR.addChangeListener(e -> config.set(configKey, startColorPBR.toSaveString()));
        startColorPBR.addChangeListener(whenChange);
        colorButton.addActionListener(e -> cWindows.add(new CCColorChooser(startColorPBR, "Stamp color", getLocation().x + getWidth() / 2, getLocation().y + getHeight() / 2, true, null,null)));
        return colorButton;
    }

    public void setEnabledAll(JComponent component, boolean enabled, JComponent... ignore) {
        setEnableSpecific(component, enabled, ignore);

        for(Component c : component.getComponents()) {
            if(c instanceof JComponent) {
                JComponent cc = (JComponent) c;
                setEnableSpecific(cc, enabled, ignore);
                if (cc.getComponents().length != 0)
                    setEnabledAll(cc, enabled, ignore);
            }
        }
    }

    private void setEnableSpecific(JComponent component, boolean enabled, JComponent... ignore) {
        boolean doDisable = true;
        for(JComponent comp : ignore)
            if(comp == component) {
                doDisable = false;
                break;
            }

        if(doDisable)
            component.setEnabled(enabled);
    }

    public JLabel createJLabel(String title, int horizontalAlignment, int verticalAlignment) {
        JLabel jlabel = new JLabel(title);
        jlabel.setHorizontalAlignment(horizontalAlignment);
        jlabel.setVerticalAlignment(verticalAlignment);
        return jlabel;
    }

    public GridLayout getGridLayoutWithMargin(int row, int cols, int hGap) {
        GridLayout layout = new GridLayout(row, cols);
        layout.setHgap(hGap);
        return layout;
    }

    @Override
    public void close() {
        for(CustomWindowListener listener : listeners)
            listener.windowClosed();
        for(CCIClosable wnd : cWindows)
            wnd.close();
        dispose();
    }

    public void addCWindow(CCIClosable cWindow) {
        cWindows.add(cWindow);
    }

    public Config getLastSelectedConfig() {
        return lastSelectedConfig;
    }

    public void addCustomWindowListener(CustomWindowListener listener) {
        listeners.add(listener);
    }

}
