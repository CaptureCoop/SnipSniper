package io.wollinger.snipsniper.sceditor;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.snipscope.SnipScopeWindow;

import java.awt.image.BufferedImage;

public class SCEditorWindow extends SnipScopeWindow {
    public SCEditorWindow(String id, BufferedImage image, int x, int y, String title, Config config, boolean isLeftToRight, String saveLocation, boolean inClipboard, boolean isStandalone) {
        super(image);
    }
}
