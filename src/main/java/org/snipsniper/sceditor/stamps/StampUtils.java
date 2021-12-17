package org.snipsniper.sceditor.stamps;

import org.snipsniper.config.Config;
import org.snipsniper.sceditor.SCEditorWindow;

public class StampUtils {
    public static final int INDEX_CUBE = 0;
    public static final int INDEX_COUNTER = 1;
    public static final int INDEX_CIRCLE = 2;
    public static final int INDEX_SIMPLE_BRUSH = 3;
    public static final int INDEX_TEXT = 4;
    public static final int INDEX_RECTANGLE = 5;
    public static final int INDEX_ERASER = 6;

    public enum TYPE {CUBE, COUNTER, CIRCLE, BRUSH, TEXT, RECTANGLE, ERASER}

    public static IStamp getNewIStampByIndex(int index, Config config, SCEditorWindow scEditorWindow) {
        switch(index) {
            case 0: return new CubeStamp(config, scEditorWindow);
            case 1: return new CounterStamp(config, scEditorWindow);
            case 2: return new CircleStamp(config, scEditorWindow);
            case 3: return new SimpleBrush(config, scEditorWindow);
            case 4: return new TextStamp(config, scEditorWindow);
            case 5: return new RectangleStamp(config, scEditorWindow);
            case 6: return new EraserStamp(scEditorWindow, config);
        }
        return null;
    }

    public static String getStampAsString(int index) {
        return getStampsAsString()[index];
    }

    public static String[] getStampsAsString() {
        return new String[]{"Marker", "Counter", "Circle", "Simple Brush", "Text", "Rectangle", "Eraser"};
    }
}
