package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.stamps.*;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.LogManager;
import io.wollinger.snipsniper.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.logging.Level;

public class SCEditorWindow extends SnipScopeWindow {
    private final String id;
    private final Config config;
    private final String title;
    private String saveLocation;
    private boolean inClipboard;

    final static int X_OFFSET = 8;

    private final IStamp[] stamps = new IStamp[6];
    private int selectedStamp = 0;

    private final RenderingHints qualityHints;

    public SCEditorWindow(String id, BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        this.id = id;
        this.config = config;
        this.title = title;
        this.saveLocation = saveLocation;
        this.inClipboard = inClipboard;

        qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        stamps[0] = new CubeStamp(this);
        stamps[1] = new CounterStamp(this);
        stamps[2] = new CircleStamp(this);
        stamps[3] = new SimpleBrush(this);
        stamps[4] = new TextStamp(this);
        stamps[5] = new RectangleStamp(this);

        SCEditorRenderer renderer = new SCEditorRenderer(this);
        SCEditorListener listener = new SCEditorListener(this);

        if(image == null)
            image = Utils.getDragPasteImage(Icons.icon_editor, "Drop image here or use CTRL + V to paste one!");

        init(image, renderer, listener);

        if (isStandalone)
            setIconImage(Icons.icon_editor);
        else
            setIconImage(Icons.icon_taskbar);

        if(!isStandalone) {
            int borderSize = config.getInt("borderSize");
            if (!isLeftToRight) borderSize = -borderSize;
            setLocation((x - X_OFFSET) + borderSize, y - getInsets().top + borderSize);

            GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
            boolean found = false;
            GraphicsConfiguration bestMonitor = null;
            final int SAFETY_OFFSET_X = 10 + config.getInt("borderSize"); //This prevents this setup not working if you do a screenshot on the top left, which would cause the location not to be in any bounds
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
                System.out.println("Setting location");
            }
        }
        setVisible(true);
        setSizeAuto();
    }

    public void refreshTitle() {
        LogManager.log(id, "Refreshing title", Level.INFO);
        String newTitle = title;
        if(saveLocation != null && !saveLocation.isEmpty())
            newTitle += " (" + saveLocation + ")";
        if(inClipboard) {
            newTitle += " (Clipboard)";
        }
        setTitle(newTitle);
    }

    public static Config getStandaloneEditorConfig() {
        return new Config("editor.cfg", "CFGE", "profile_defaults.cfg");
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

    public Color getCensorColor() {
        return Color.BLACK;
    }

    public Map<?,?> getQualityHints() {
        return qualityHints;
    }
}
