package net.snipsniper.utils

class SSFile {
    val path: String
    val location: LOCATION
    enum class LOCATION {JAR, LOCAL}

    constructor(path: String, location: LOCATION) {
        this.path = path
        this.location = location
    }

    constructor(path: String) {
        if(path.contains(JAR_IDENTIFIER)) {
            location = LOCATION.JAR
            this.path = path.replace(JAR_IDENTIFIER, "")
        } else if(path.contains(LOCAL_IDENTIFIER)) {
            location = LOCATION.LOCAL
            this.path = path.replace(LOCAL_IDENTIFIER, "")
        } else {
            this.path = path
            location = LOCATION.JAR
        }
    }

    fun getPathWithLocation(): String = if(location == LOCATION.JAR) JAR_IDENTIFIER + path else LOCAL_IDENTIFIER + path

    override fun toString(): String = "SSFile path: $path, location: ${location.toString().lowercase()}"

    companion object {
        const val JAR_IDENTIFIER = "%%JAR%%"
        const val LOCAL_IDENTIFIER = "%%LOCAL%%"
    }
}