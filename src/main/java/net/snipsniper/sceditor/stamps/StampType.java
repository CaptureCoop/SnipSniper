package net.snipsniper.sceditor.stamps;

import net.snipsniper.config.Config;
import net.snipsniper.sceditor.SCEditorWindow;

import java.util.HashMap;

public enum StampType {
    CUBE("Cube"),
    COUNTER("Counter"),
    CIRCLE("Circle"),
    SIMPLE_BRUSH("Simple Brush"),
    TEXT("Text"),
    RECTANGLE("Rectangle"),
    ERASER("Eraser");

    private static final HashMap<StampType, Integer> indexes = new HashMap<StampType, Integer>();
    private final String title;

    StampType(String title) {
        this.title = title;
    }

    //This way we can rearrange enums without worrying about updating their index manually
    public int getIndex() {
        if(indexes.containsKey(this))
            return indexes.get(this);

        int index = 0;
        for(StampType type : StampType.values()) {
            if (type == this) {
                indexes.put(this, index);
                return index;
            }
            index++;
        }
        return -1;
    }

    public String getTitle() {
        return title;
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
