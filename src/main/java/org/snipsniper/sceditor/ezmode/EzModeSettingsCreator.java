package org.snipsniper.sceditor.ezmode;

import javax.swing.*;
import org.snipsniper.sceditor.stamps.IStamp;

public class EzModeSettingsCreator {
    public static void addSettingsToPanel(JPanel panel, IStamp stamp) {
        switch(stamp.getType()) {
            case CUBE: break;
            case COUNTER: break;
            case CIRCLE: break;
            case BRUSH: break;
            case TEXT: break;
            case RECTANGLE: break;
            case ERASER: break;
        }
    }
}
