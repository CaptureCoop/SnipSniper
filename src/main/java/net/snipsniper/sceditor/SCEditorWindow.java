package net.snipsniper.sceditor;

import net.snipsniper.ImageManager;
import net.snipsniper.LogManager;
import net.snipsniper.SnipSniper;
import net.snipsniper.StatsManager;
import net.snipsniper.config.Config;
import net.snipsniper.config.ConfigHelper;
import net.snipsniper.sceditor.stamps.*;
import net.snipsniper.utils.IClosable;
import net.snipsniper.utils.ImageUtils;
import org.capturecoop.ccutils.utils.StringUtils;
import net.snipsniper.utils.Utils;
import net.snipsniper.configwindow.ConfigWindow;
import net.snipsniper.sceditor.ezmode.EzModeSettingsCreator;
import net.snipsniper.sceditor.ezmode.EzModeStampTab;
import net.snipsniper.snipscope.SnipScopeWindow;
import org.apache.commons.lang3.SystemUtils;
import net.snipsniper.utils.enums.LogLevel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class SCEditorWindow extends SnipScopeWindow implements IClosable {
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
    private final EzModeSettingsCreator ezModeSettingsCreator = new EzModeSettingsCreator(this);
    private int ezModeWidth = 200;
    private int ezModeHeight = 40;
    private final JPanel ezModeStampPanel = new JPanel();
    private final JPanel ezModeTitlePanel = new JPanel();
    private final JLabel ezModeTitle = new JLabel("Marker");
    private final JPanel ezModeStampSettingsPanel = new JPanel();
    private final JScrollPane ezModeStampSettingsScrollPane;

    private final JTabbedPane ezModeStampPanelTabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    public SCEditorWindow(BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        instance = this;
        this.config = config;
        this.title = title;
        this.saveLocation = saveLocation;
        this.inClipboard = inClipboard;

        image = ImageUtils.ensureAlphaLayer(image);

        ezMode = config.getBool(ConfigHelper.PROFILE.ezMode);

        LogManager.log("Creating new editor window...", LogLevel.INFO);

        StatsManager.incrementCount(StatsManager.EDITOR_STARTED_AMOUNT);

        qualityHints = Utils.getRenderingHints();

        LogManager.log("Loading stamps", LogLevel.INFO);

        stamps[0] = new CubeStamp(config, this);
        stamps[1] = new CounterStamp(config, this);
        stamps[2] = new CircleStamp(config, this);
        stamps[3] = new SimpleBrush(config, this);
        stamps[4] = new TextStamp(config, this);
        stamps[5] = new RectangleStamp(config, this);
        stamps[6] = new EraserStamp(this, config);

        if(image == null) {
            if (config.getBool(ConfigHelper.PROFILE.standaloneStartWithEmpty)) {
                Dimension imgSize = Toolkit.getDefaultToolkit().getScreenSize();
                image = new BufferedImage(imgSize.width / 2, imgSize.height / 2, BufferedImage.TYPE_INT_RGB);
                Graphics imgG = image.getGraphics();
                imgG.setColor(Color.WHITE);
                imgG.fillRect(0, 0, image.getWidth(), image.getHeight());
                imgG.dispose();
            } else {
                image = ImageUtils.getDragPasteImage(ImageManager.getImage("icons/editor.png"), "Drop image here or use CTRL + V to paste one!");
                defaultImage = image;
            }
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
        Rectangle[] tabRects = new Rectangle[ezModeStampPanelTabs.getTabCount()];
        //TODO: Make this dynamic if we ever allow resizing
        for(int i = 0; i < tabRects.length; i++)
            tabRects[i] = ezModeStampPanelTabs.getUI().getTabBounds(ezModeStampPanelTabs, i);

        ezModeStampPanelTabs.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                for(int i = 0; i < tabRects.length; i++) {
                    if(tabRects[i].contains(e.getPoint())) {
                        setEzModeTitle(StampUtils.getStampAsString(i));
                        break;
                    }
                }
            }
        });

        ezModeStampPanelTabs.addChangeListener(e -> {
            setSelectedStamp(ezModeStampPanelTabs.getSelectedIndex());
            instance.requestFocus();
        });

        ezModeStampPanel.setLayout(null);
        ezModeStampPanel.add(ezModeStampPanelTabs);

        ezModeTitle.setHorizontalAlignment(JLabel.CENTER);
        ezModeTitle.setVerticalAlignment(JLabel.CENTER);
        ezModeTitlePanel.add(ezModeTitle);

        ezModeSettingsCreator.addSettingsToPanel(ezModeStampSettingsPanel, getSelectedStamp());

        ezModeStampSettingsScrollPane = new JScrollPane(ezModeStampSettingsPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        ezModeStampSettingsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        ezModeStampSettingsScrollPane.getVerticalScrollBar().setUnitIncrement(10);
        ezModeStampSettingsScrollPane.setWheelScrollingEnabled(true);

        add(ezModeStampPanel);
        add(ezModeStampSettingsScrollPane);
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
            JMenuItem whatsappTest = new JMenuItem("Border test");
            whatsappTest.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int borderThickness = 10;
                    //Fix to have this work without originalImage. As we will remove/Change this anyways i dont care if this affects anything for now.
                    BufferedImage imageToUse = getImage();
                    BufferedImage test = new BufferedImage(imageToUse.getWidth() + borderThickness, imageToUse.getHeight() + borderThickness, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D) test.getGraphics();
                    g.setRenderingHints(qualityHints);
                    for(int y = 0; y < imageToUse.getHeight(); y++) {
                        for(int x = 0; x < imageToUse.getWidth(); x++) {
                            if(new Color(imageToUse.getRGB(x, y), true).getAlpha() > 10) {
                                g.setColor(Color.WHITE);
                                g.fillOval((x + borderThickness / 2) - borderThickness / 2, (y + borderThickness / 2) - borderThickness / 2, borderThickness, borderThickness);
                            }
                        }
                    }
                    g.drawImage(imageToUse, borderThickness / 2 , borderThickness / 2, imageToUse.getWidth(), imageToUse.getHeight(), null);
                    g.dispose();

                    setImage(test, true, true);
                    isDirty = true;
                    repaint();
                    refreshTitle();
                }
            });
            topBar.add(whatsappTest);
            JMenuItem whatsappBox = new JMenuItem("Box test");
            whatsappBox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int width = 512;
                    int height = 512;
                    BufferedImage test = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g = (Graphics2D) test.getGraphics();
                    g.setRenderingHints(qualityHints);

                    Dimension optimalDimension = Utils.getScaledDimension(originalImage, new Dimension(width, height));
                    g.drawImage(originalImage, test.getWidth() / 2 - optimalDimension.width / 2, test.getHeight() / 2 - optimalDimension.height / 2, optimalDimension.width, optimalDimension.height, null);

                    g.dispose();
                    setImage(test, true, true);
                    isDirty = true;
                    repaint();
                    refreshTitle();
                }
            });
            topBar.add(whatsappBox);
            setJMenuBar(topBar);
        }

        if(ezMode) {
            setSize(getWidth() + ezModeWidth, getHeight() + ezModeHeight);
            setLocation((int)getLocation().getX() - ezModeWidth, (int)getLocation().getY() - ezModeHeight);
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
        requestFocus();
        LogManager.log("Started new editor window. (%c)", LogLevel.INFO, this);
    }

    public void addEZModeStampButton(String title, String iconName, String theme, int stampIndex) {
        ezModeStampPanelTabs.addTab(title, null);
        BufferedImage ezIconMarker = ImageManager.getImage(StringUtils.format("ui/editor/%c/%c.png", theme, iconName));
        ezModeStampPanelTabs.setTabComponentAt(stampIndex, new EzModeStampTab(ezIconMarker, 32, this, stampIndex));
        ezModeStampPanelTabs.setIconAt(stampIndex, new ImageIcon(ezIconMarker));
    }

    @Override
    public void resizeTrigger() {
        super.resizeTrigger();

        if(ezMode) {
            int titleMargin = 5;
            int ezModeWidthToUse = ezModeWidth;
            if(ezModeStampSettingsScrollPane.getVerticalScrollBar().isVisible())
                ezModeWidthToUse += ezModeStampSettingsScrollPane.getVerticalScrollBar().getWidth();

            ezModeTitlePanel.setBounds(0, 0, ezModeWidthToUse, ezModeHeight);
            ezModeTitle.setFont(new Font("Arial", Font.PLAIN, ezModeHeight - titleMargin));
            ezModeStampPanel.setBounds(ezModeWidthToUse, 0, getContentPane().getWidth() - ezModeWidthToUse, ezModeHeight);
            ezModeStampPanelTabs.setBounds(0, 0, ezModeStampPanel.getWidth(), ezModeStampPanel.getHeight());

            int ezModeSettingsHeight = ezModeSettingsCreator.getLastCorrectHeight();
            ezModeStampSettingsPanel.setPreferredSize(new Dimension(ezModeWidthToUse, ezModeSettingsHeight));
            ezModeStampSettingsPanel.setMinimumSize(new Dimension(ezModeWidthToUse, ezModeSettingsHeight));
            ezModeStampSettingsPanel.setMaximumSize(new Dimension(ezModeWidthToUse, ezModeSettingsHeight));

            ezModeStampSettingsScrollPane.setBounds(0, ezModeHeight, ezModeWidthToUse, getContentPane().getHeight() - ezModeHeight);

            renderer.setBounds(ezModeWidthToUse, ezModeHeight, getContentPane().getWidth() - ezModeWidthToUse, getContentPane().getHeight() - ezModeHeight);
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
        if(location != null) {
            String folder = location.replace(new File(location).getName(), "");
            config.set(ConfigHelper.PROFILE.lastSaveFolder, folder);
            config.save();
        }
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
        newTitle += StringUtils.format(" %cx%c", getImage().getWidth(), getImage().getHeight());
        setTitle(newTitle);
    }

    public void setImage(BufferedImage image, boolean resetHistory, boolean isNewImage) {
        image = ImageUtils.ensureAlphaLayer(image);
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
        if(selectedStamp == i)
            return;
        selectedStamp = i;
        ezModeStampPanelTabs.setSelectedIndex(i);
        setEzModeTitle(StampUtils.getStampAsString(i));
        updateEzUI(true);
    }

    public void updateEzUI(boolean reset) {
        if(ezMode && reset)
            ezModeSettingsCreator.addSettingsToPanel(ezModeStampSettingsPanel, getSelectedStamp());
        else if(!ezMode && reset)
            ezModeStampSettingsPanel.removeAll();
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
        updateEzUI(true);
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
