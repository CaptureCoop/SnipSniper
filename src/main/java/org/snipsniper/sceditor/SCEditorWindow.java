package org.snipsniper.sceditor;

import org.snipsniper.ImageManager;
import org.snipsniper.SnipSniper;
import org.snipsniper.StatsManager;
import org.snipsniper.config.Config;
import org.snipsniper.configwindow.ConfigWindow;
import org.snipsniper.sceditor.ezmode.EzModeSettingsCreator;
import org.snipsniper.sceditor.ezmode.EzModeStampTab;
import org.snipsniper.sceditor.stamps.*;
import org.snipsniper.snipscope.SnipScopeWindow;
import org.apache.commons.lang3.SystemUtils;
import org.snipsniper.config.ConfigHelper;
import org.snipsniper.utils.*;
import org.snipsniper.LogManager;
import org.snipsniper.utils.enums.LogLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;

public class SCEditorWindow extends SnipScopeWindow implements IClosable{
    private final SCEditorWindow instance;
    private final Config config;
    private final String title;
    private String saveLocation;
    private boolean inClipboard;
    private BufferedImage originalImage;

    private final static int X_OFFSET = 8;

    private final IStamp[] stamps = new IStamp[7];
    private int selectedStamp = 0;

    private final SCEditorListener listener;
    private final SCEditorRenderer renderer;

    public boolean isDirty = false;

    private final RenderingHints qualityHints;

    public static final String FILENAME_MODIFIER = "_edited";

    private BufferedImage defaultImage;

    private final ArrayList<IClosable> cWindows = new ArrayList<>();

    private boolean isStampVisible = true;

    private boolean ezMode;
    private int ezModeWidth = 200;
    private int ezModeHeight = 40;
    private final JPanel ezModeStampPanel = new JPanel();
    private final JPanel ezModeTitlePanel = new JPanel();
    private final JLabel ezModeTitle = new JLabel("Marker");
    private final JPanel ezModeStampSettingsPanel = new JPanel();
    private final JTabbedPane ezModeStampPanelTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    
    public SCEditorWindow(BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        instance = this;
        this.config = config;
        this.title = title;
        this.saveLocation = saveLocation;
        this.inClipboard = inClipboard;

        LogManager.log("Starting new editor window. (" + this + ")", LogLevel.INFO);

        StatsManager.incrementCount(StatsManager.EDITOR_STARTED_AMOUNT);

        qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LogManager.log("Loading stamps", LogLevel.INFO);

        stamps[0] = new CubeStamp(config, this);
        stamps[1] = new CounterStamp(config);
        stamps[2] = new CircleStamp(config);
        stamps[3] = new SimpleBrush(config, this);
        stamps[4] = new TextStamp(config, this);
        stamps[5] = new RectangleStamp(config);
        stamps[6] = new EraserStamp(this, config);

        if(image == null) {
            image = ImageUtils.getDragPasteImage(ImageManager.getImage("icons/editor.png"), "Drop image here or use CTRL + V to paste one!");
            defaultImage = image;
        }
        renderer = new SCEditorRenderer(this);
        listener = new SCEditorListener(this);

        originalImage = ImageUtils.copyImage(image);
        init(image, renderer, listener);
        setLayout(null);

        String ezIconType = "black";
        if(SnipSniper.getConfig().getString(ConfigHelper.MAIN.theme).equals("dark")) {
            ezIconType = "white";
        }

        addEZModeStampButton("Marker", "marker", ezIconType, 0);
        addEZModeStampButton("Counter", "counter", ezIconType, 1);
        addEZModeStampButton("Circle", "circle", ezIconType, 2);
        addEZModeStampButton("Brush", "brush", ezIconType, 3);
        addEZModeStampButton("Text", "text_tool", ezIconType, 4);
        addEZModeStampButton("Rectangle", "rectangle", ezIconType, 5);
        addEZModeStampButton("Eraser", "ratzefummel", ezIconType, 6);

        ezModeStampPanelTabs.addChangeListener(e -> {
            setSelectedStamp(ezModeStampPanelTabs.getSelectedIndex());
            instance.requestFocus();
        });

        ezModeStampPanel.setLayout(null);
        ezModeStampPanel.add(ezModeStampPanelTabs);

        ezModeTitle.setHorizontalAlignment(JLabel.CENTER);
        ezModeTitle.setVerticalAlignment(JLabel.CENTER);
        ezModeTitlePanel.add(ezModeTitle);

        EzModeSettingsCreator.addSettingsToPanel(this, ezModeStampSettingsPanel, getSelectedStamp());

        add(ezModeStampPanel);
        add(ezModeStampSettingsPanel);
        add(ezModeTitlePanel);

        listener.resetHistory();

        setIconImage(ImageManager.getImage("icons/editor.png"));

        setFocusTraversalKeysEnabled(false);
        setVisible(true);

        if(!(x < 0 && y < 0)) {
            int borderSize = config.getInt(ConfigHelper.PROFILE.borderSize);
            if (!isLeftToRight) borderSize = -borderSize;
            setLocation((x - X_OFFSET) + borderSize, y - getInsets().top + borderSize);
            LogManager.log("Setting location to " + getLocation(), LogLevel.INFO);
        }

        refreshTitle();
        setSizeAuto();
        if(x < 0 && y < 0)
            setLocationAuto();

        if(SystemUtils.IS_OS_WINDOWS) {
            JMenuBar topBar = new JMenuBar();
            JMenuItem configItem = new JMenuItem("Config");
            configItem.addActionListener(e -> cWindows.add(new ConfigWindow(config, ConfigWindow.PAGE.editorPanel)));
            topBar.add(configItem);
            JMenuItem newItem = new JMenuItem("New");
            newItem.addActionListener(e -> openNewImageWindow());
            topBar.add(newItem);
            setJMenuBar(topBar);
        }

        if(!isStandalone) {
            GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
            boolean found = false;
            GraphicsConfiguration bestMonitor = null;
            final int SAFETY_OFFSET_X = 10 + config.getInt(ConfigHelper.PROFILE.borderSize); //This prevents this setup not working if you do a screenshot on the top left, which would cause the location not to be in any bounds
            for (GraphicsDevice gd : localGE.getScreenDevices()) {
                for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
                    if(!found) {
                        Rectangle bounds = graphicsConfiguration.getBounds();

                        Point testLocation = new Point((int) (getLocation().getX() + SAFETY_OFFSET_X), (int) getLocation().getY());

                        if (bounds.contains(testLocation))
                            found = true;

                        if (testLocation.getX() > bounds.getX() && testLocation.getX() < (bounds.getX() + bounds.getWidth()) && bestMonitor == null) {
                            bestMonitor = graphicsConfiguration;
                        }
                    }
                }
            }

            if(!found && bestMonitor != null) {
                setLocation((int) getLocation().getX(), bestMonitor.getBounds().y);
            }
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if(isStandalone)
                    SnipSniper.exit(false);
                close();
            }
        });
        setEnableInteraction(!isDefaultImage());
    }

    public void addEZModeStampButton(String title, String iconName, String theme, int stampIndex) {
        ezModeStampPanelTabs.addTab(title, null);
        BufferedImage ezIconMarker = ImageManager.getImage("ui/editor/" + theme + "/" + iconName + ".png");
        ezModeStampPanelTabs.setTabComponentAt(stampIndex, new EzModeStampTab(ezIconMarker, 32, this, stampIndex));
        ezModeStampPanelTabs.setIconAt(stampIndex, new ImageIcon(ezIconMarker));
    }

    @Override
    public void resizeTrigger() {
        super.resizeTrigger();

        if(ezMode) {
            int titleMargin = 5;

            ezModeTitlePanel.setBounds(0, 0, ezModeWidth, ezModeHeight);
            ezModeTitle.setFont(new Font("Arial", Font.PLAIN, ezModeHeight - titleMargin));
            ezModeStampPanel.setBounds(ezModeWidth, 0, getContentPane().getWidth() - ezModeWidth, ezModeHeight);
            ezModeStampPanelTabs.setBounds(0, 0, ezModeStampPanel.getWidth(), ezModeStampPanel.getHeight());
            ezModeStampSettingsPanel.setBounds(0, ezModeHeight, ezModeWidth, getContentPane().getHeight() - ezModeHeight);
            renderer.setBounds(ezModeWidth, ezModeHeight, getContentPane().getWidth() - ezModeWidth, getContentPane().getHeight() - ezModeHeight);
        } else {
            ezModeTitlePanel.setBounds(0, 0, 0, 0);
            ezModeStampPanel.setBounds(0, 0, 0, 0);
            renderer.setBounds(0, 0, getContentPane().getWidth(), getContentPane().getHeight());
            instance.requestFocus();
        }
    }

    public void openNewImageWindow() {
        NewImageWindow window = new NewImageWindow();
        cWindows.add(window);
        int posX = (int) (getLocation().getX() + getWidth() / 2) - window.getWidth() / 2;
        int posY = (int) (getLocation().getY() + getHeight() / 2) - window.getHeight() / 2;
        window.setLocation(posX, posY);
        window.setOnSubmit(args -> {
            setImage(window.getImage(), true, true);
            isDirty = true;
            repaint();
        });
    }

    public void saveImage() {
        BufferedImage image = getImage();
        BufferedImage finalImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = finalImg.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
        g.dispose();
        String location = ImageUtils.saveImage(finalImg, config.getString(ConfigHelper.PROFILE.saveFormat), FILENAME_MODIFIER, config);
        if(location != null)
            config.set(ConfigHelper.PROFILE.lastSaveFolder, location);
        if(config.getBool(ConfigHelper.PROFILE.copyToClipboard))
            ImageUtils.copyToClipboard(finalImg);
    }

    public void refreshTitle() {
        LogManager.log("Refreshing title", LogLevel.INFO);
        String newTitle = title;
        if(saveLocation != null && !saveLocation.isEmpty())
            newTitle += " (" + saveLocation + ")";
        if(inClipboard) {
            newTitle += " (Clipboard)";
        }
        setTitle(newTitle);
    }

    public void setImage(BufferedImage image, boolean resetHistory, boolean isNewImage) {
        super.setImage(image);
        LogManager.log("Setting new Image", LogLevel.INFO);
        setEnableInteraction(!isDefaultImage());

        if(listener != null && resetHistory) {
            listener.resetHistory();
            for(IStamp stamp : stamps)
                stamp.reset();
        }

        if(isNewImage) {
            resetZoom();
            renderer.resetPreview();
            originalImage = ImageUtils.copyImage(image);
        }
    }

    public static Config getStandaloneEditorConfig() {
        return new Config("editor.cfg", "profile_defaults.cfg");
    }

    public void setSaveLocation(String saveLocation) {
        this.saveLocation = saveLocation;
    }

    public void setInClipboard(boolean inClipboard) {
        this.inClipboard = inClipboard;
    }

    public IStamp getSelectedStamp() {
        return stamps[selectedStamp];
    }

    public void setEzModeTitle(String title) {
        if(title == null) {
            ezModeTitle.setText(StampUtils.getStampAsString(selectedStamp));
            return;
        }
        ezModeTitle.setText(title);
    }

    public void setSelectedStamp(int i) {
        selectedStamp = i;
        ezModeStampPanelTabs.setSelectedIndex(i);
        setEzModeTitle(StampUtils.getStampAsString(i));
        EzModeSettingsCreator.addSettingsToPanel(this, ezModeStampSettingsPanel, getSelectedStamp());
    }

    public IStamp[] getStamps() {
        return stamps;
    }

    public Config getConfig() {
        return config;
    }

    public Map<?,?> getQualityHints() {
        return qualityHints;
    }

    public String toString() {
        return StringUtils.format("SCEditorWindow Pos:[%c] Path:[%c]", getLocation(), saveLocation);
    }

    public void setEzMode(boolean value) {
        ezMode = value;
        resizeTrigger();
    }

    public boolean isDefaultImage() {
        return defaultImage == getImage();
    }

    public boolean isEzMode() {
        return ezMode;
    }

    public BufferedImage getOriginalImage() {
        return originalImage;
    }

    public void addClosableWindow(IClosable wnd) {
        cWindows.add(wnd);
    }

    @Override
    public void close() {
        for(IClosable wnd : cWindows)
            wnd.close();
    }

    public void setStampVisible(boolean enabled) {
        isStampVisible = enabled;
    }

    public boolean isStampVisible() {
        return isStampVisible;
    }

    public int getEzModeWidth() {
        return ezModeWidth;
    }

    public int getEzModeHeight() {
        return ezModeHeight;
    }
}
