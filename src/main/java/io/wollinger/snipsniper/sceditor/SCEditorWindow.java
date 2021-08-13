package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.configwindow.ConfigWindow;
import io.wollinger.snipsniper.sceditor.stamps.*;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;
import io.wollinger.snipsniper.snipscope.ui.SnipScopeUIComponent;
import io.wollinger.snipsniper.utils.*;
import org.apache.commons.lang3.SystemUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Level;

public class SCEditorWindow extends SnipScopeWindow {
    private final Config config;
    private final String title;
    private String saveLocation;
    private boolean inClipboard;

    final static int X_OFFSET = 8;

    private final IStamp[] stamps = new IStamp[6];
    private int selectedStamp = 0;

    private final SCEditorListener listener;
    private final SCEditorRenderer renderer;

    public boolean isDirty = false;

    private final RenderingHints qualityHints;

    public static final String FILENAME_MODIFIER = "_edited";

    public SCEditorWindow(String id, BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        super(id);
        this.config = config;
        this.title = title;
        this.saveLocation = saveLocation;
        this.inClipboard = inClipboard;

        LogManager.log(id, "Starting new editor window. (" + this + ")", Level.INFO);

        qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        LogManager.log(id, "Loading stamps", Level.INFO);

        stamps[0] = new CubeStamp(config, this);
        stamps[1] = new CounterStamp(config);
        stamps[2] = new CircleStamp(config);
        stamps[3] = new SimpleBrush(config, this);
        stamps[4] = new TextStamp(config, this);
        stamps[5] = new RectangleStamp(config);

        if(image == null)
            image = Utils.getDragPasteImage(Icons.getImage("icons/editor.png"), "Drop image here or use CTRL + V to paste one!");

        renderer = new SCEditorRenderer(this);
        listener = new SCEditorListener(this);

        init(image, renderer, listener);

        listener.resetHistory();

        setIconImage(Icons.getImage("icons/editor.png"));

        setFocusTraversalKeysEnabled(false);
        setVisible(true);

        if(!(x < 0 && y < 0)) {
            int borderSize = config.getInt(ConfigHelper.PROFILE.borderSize);
            if (!isLeftToRight) borderSize = -borderSize;
            setLocation((x - X_OFFSET) + borderSize, y - getInsets().top + borderSize);
            LogManager.log(getID(), "Setting location to " + getLocation(), Level.INFO);
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
        refreshTitle();
        setSizeAuto();
        if(x < 0 && y < 0)
            setLocationAuto();

        if(SystemUtils.IS_OS_WINDOWS) {
            JMenuBar topBar = new JMenuBar();
            JMenuItem configItem = new JMenuItem("Config");
            configItem.addActionListener(e -> new ConfigWindow(config, ConfigWindow.PAGE.editorPanel));
            topBar.add(configItem);
            setJMenuBar(topBar);
        }
    }

    public void saveImage() {
        BufferedImage image = getImage();
        BufferedImage finalImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = finalImg.getGraphics();
        g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
        g.dispose();
        Utils.saveImage(getID(), finalImg, FILENAME_MODIFIER, config);
        if(config.getBool(ConfigHelper.PROFILE.copyToClipboard))
            Utils.copyToClipboard(getID(),finalImg);
    }

    public void refreshTitle() {
        LogManager.log(getID(), "Refreshing title", Level.INFO);
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
        LogManager.log(getID(), "Setting new Image", Level.INFO);

        if(listener != null && resetHistory) {
            listener.resetHistory();
            for(IStamp stamp : stamps)
                stamp.reset();
        }

        if(isNewImage) {
            resetZoom();
            renderer.resetPreview();
        }
    }

    public static Config getStandaloneEditorConfig() {
        return new Config("editor.cfg", "CFGE", "profile_defaults.cfg");
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

    public void setSelectedStamp(int i) {
        selectedStamp = i;
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
        return Utils.formatArgs("SCEditorWindow ID:[{0}] Pos:[{1}] Path:[{2}]", getID(), getLocation(), saveLocation);
    }
}
