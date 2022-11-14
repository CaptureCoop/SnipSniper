package net.snipsniper.sceditor.stamps

import net.snipsniper.config.Config
import net.snipsniper.sceditor.SCEditorWindow

enum class StampType(val title: String, val iconFile: String) {
    CUBE("Marker", "marker"),
    COUNTER("Counter", "counter"),
    CIRCLE("Circle", "circle"),
    RECTANGLE("Rectangle", "rectangle"),
    SIMPLE_BRUSH("Simple Brush", "brush"),
    TEXT("Text", "text_tool"),
    ERASER("Eraser", "ratzefummel");

    //This way we can rearrange enums without worrying about updating their index manually
    val index: Int
        get() {
            if (indexCache.containsKey(this)) return indexCache[this]!!
            values().forEachIndexed { index, type ->
                if (type == this) return index
                indexCache[type] = index
            }
            return -1
        }

    fun getIStamp(config: Config, scEditorWindow: SCEditorWindow?) = when (this) {
        CUBE -> CubeStamp(config, scEditorWindow)
        COUNTER -> CounterStamp(config)
        CIRCLE -> CircleStamp(config)
        SIMPLE_BRUSH -> SimpleBrush(config, scEditorWindow)
        TEXT -> TextStamp(config, scEditorWindow)
        RECTANGLE -> RectangleStamp(config)
        ERASER -> EraserStamp(scEditorWindow, config)
    }

    companion object {
        private val indexCache = HashMap<StampType, Int>()
        fun getByIndex(index: Int) = values()[index]
        val size: Int get() = values().size
    }
}