package net.snipsniper.config

class ConfigContainer {
    var list = ArrayList<ConfigOption>()
        private set
    private var map = HashMap<String, ConfigOption>()

    fun set(key: String, value: String) {
        val option = ConfigOption(key, value)
        if(!map.containsKey(key)) {
            list.add(option)
            map[key] = option
        } else {
            list.filter { it.type == ConfigOption.TYPE.KEY_VALUE && it.key == key }.forEach { it.value = value }
            map.replace(key, option)
        }
    }

    fun set(comment: String) = list.add(ConfigOption(comment))
    fun addNewLine() = list.add(ConfigOption())
    fun get(key: String): String? = map[key]?.value
    fun containsKey(key: String): Boolean = map.containsKey(key)
    fun isEmpty(): Boolean = map.isEmpty()
    fun clear() {
        list.clear()
        map.clear()
    }

    fun loadFromContainer(container: ConfigContainer) {
        clear()
        container.list.forEach {
            when(it.type) {
                ConfigOption.TYPE.KEY_VALUE -> set(it.key, it.value)
                ConfigOption.TYPE.COMMENT -> set(it.value)
                ConfigOption.TYPE.NEWLINE -> addNewLine()
            }
        }
    }

    override fun equals(other: Any?) = if(other !is ConfigContainer) false else list.none { it.key.isNotEmpty() && other.get(it.key) != it.value }

    override fun hashCode() = 31 * list.hashCode() + map.hashCode()
}