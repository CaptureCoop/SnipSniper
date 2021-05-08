package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.stamps.*;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Map;

public class SCEditorWindow extends SnipScopeWindow {
    private final String id;
    private final Config config;

    final static int X_OFFSET = 8;

    private final IStamp[] stamps = new IStamp[6];
    private int selectedStamp = 0;

    private final RenderingHints qualityHints;

    public SCEditorWindow(String id, BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        this.id = id;
        this.config = config;

        qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        stamps[0] = new CubeStamp(this);
        stamps[1] = new CounterStamp(config);
        stamps[2] = new CircleStamp(config);
        stamps[3] = new SimpleBrush(this);
        stamps[4] = new TextStamp(config);
        stamps[5] = new RectangleStamp(config);

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
        }
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
