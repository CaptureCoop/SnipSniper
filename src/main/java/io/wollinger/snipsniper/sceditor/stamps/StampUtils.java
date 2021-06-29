package io.wollinger.snipsniper.sceditor.stamps;

import io.wollinger.snipsniper.Config;
import io.wollinger.snipsniper.sceditor.SCEditorWindow;

public class StampUtils {
    public static final int INDEX_CUBE = 0;
    public static final int INDEX_COUNTER = 1;
    public static final int INDEX_CIRCLE = 2;
    public static final int INDEX_SIMPLE_BRUSH = 3;
    public static final int INDEX_TEXT = 4;
    public static final int INDEX_RECTANGLE = 5;

    public static IStamp getNewIStampByIndex(int index, Config config, SCEditorWindow scEditorWindow) {
        switch(index) {
            case 0: return new CubeStamp(config, scEditorWindow);
            case 1: return new CounterStamp(config);
            case 2: return new CircleStamp(config);
            case 3: return new SimpleBrush(config, scEditorWindow);
            case 4: return new TextStamp(config, scEditorWindow);
            case 5: return new RectangleStamp(config);
        }
        return null;
    }

    public static String[] getStampsAsString() {
        return new String[]{"Cube", "Counter", "Circle", "Simple Brush", "Text", "Rectangle"};
    }
}
