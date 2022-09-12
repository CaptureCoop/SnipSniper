package net.snipsniper.config

class ConfigOption {
    enum class TYPE {KEY_VALUE, COMMENT, NEWLINE}
    val key: String
    var value: String
    val type: TYPE

    constructor(key: String, value: String) {
        this.key = key
        this.value = value
        type = TYPE.KEY_VALUE
    }

    constructor(comment: String) {
        key = ""; value = comment
        type = TYPE.COMMENT
    }

    constructor() {
        key = ""; value = ""
        type = TYPE.NEWLINE
    }

    override fun toString(): String {
        return when (type) {
            TYPE.KEY_VALUE -> "$key=$value"
            TYPE.COMMENT -> "#$value"
            TYPE.NEWLINE -> ""
        }
    }
}