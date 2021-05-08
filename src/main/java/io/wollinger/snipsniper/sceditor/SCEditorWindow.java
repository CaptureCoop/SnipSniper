package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;
import io.wollinger.snipsniper.utils.Icons;
import io.wollinger.snipsniper.utils.Utils;

import java.awt.image.BufferedImage;

public class SCEditorWindow extends SnipScopeWindow {
    private final String id;
    private final Config config;

    final static int X_OFFSET = 8;

    public SCEditorWindow(String id, BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        super(image);
        this.id = id;
        this.config = config;

        if (isStandalone)
            setIconImage(Icons.icon_editor);
        else
            setIconImage(Icons.icon_taskbar);

        int borderSize = config.getInt("borderSize");
        if(!isLeftToRight) borderSize = -borderSize;
        setLocation((x - X_OFFSET) + borderSize, y - getInsets().top + borderSize);
    }


}
