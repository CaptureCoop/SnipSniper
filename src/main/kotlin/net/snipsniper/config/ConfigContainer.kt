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
            list.forEach {
                if(it.type == ConfigOption.TYPE.KEY_VALUE && it.key == key) {
                    it.value = value
                    return@forEach
                }
            }
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

    override fun equals(other: Any?): Boolean {
        if(other !is ConfigContainer) return false
        var isSame = true
        list.forEach {
            if(it.key.isNotEmpty()) {
                val otherValue = other.get(it.key)
                if(otherValue != null && otherValue != it.value) {
                    isSame = false
                }
            }
        }
        return isSame
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
}