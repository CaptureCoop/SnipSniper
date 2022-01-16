package net.snipsniper.sceditor.stamps;

import net.snipsniper.config.Config;
import net.snipsniper.sceditor.SCEditorWindow;

import java.util.HashMap;

public enum StampType {
    CUBE("Marker", "marker"),
    COUNTER("Counter", "counter"),
    CIRCLE("Circle", "circle"),
    RECTANGLE("Rectangle", "rectangle"),
    SIMPLE_BRUSH("Simple Brush", "brush"),
    TEXT("Text", "text_tool"),
    ERASER("Eraser", "ratzefummel");

    private static final HashMap<StampType, Integer> indexCache = new HashMap<>();
    private final String title;
    private final String iconFile;

    StampType(String title, String iconFile) {
        this.title = title;
        this.iconFile = iconFile;
    }

    //This way we can rearrange enums without worrying about updating their index manually
    public int getIndex() {
        if(indexCache.containsKey(this))
            return indexCache.get(this);

        int index = 0;
        int foundIndex = -1;
        for(StampType type : StampType.values()) {
            if (type == this)
                foundIndex = index;
            indexCache.put(type, index);
            index++;
        }
        return foundIndex;
    }

    public String getTitle() {
        return title;
    }

    public String getIconFile() {
        return iconFile;
    }

    public IStamp getIStamp(Config config, SCEditorWindow scEditorWindow) {
        switch(this) {
            case CUBE: return new CubeStamp(config, scEditorWindow);
            case COUNTER: return new CounterStamp(config, scEditorWindow);
            case CIRCLE: return new CircleStamp(config, scEditorWindow);
            case SIMPLE_BRUSH: return new SimpleBrush(config, scEditorWindow);
            case TEXT: return new TextStamp(config, scEditorWindow);
            case RECTANGLE: return new RectangleStamp(config, scEditorWindow);
            case ERASER: return new EraserStamp(scEditorWindow, config);
        }
        return null;
    }

    public static StampType getByIndex(int index) {
        return StampType.values()[index];
    }

    public static int getSize() {
        return StampType.values().length;
    }
}
