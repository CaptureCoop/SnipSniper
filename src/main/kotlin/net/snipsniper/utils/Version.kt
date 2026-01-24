package net.snipsniper.utils

class Version(versionString: String) {
    val regex = Regex("""(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)(?:-(?<tag>[0-9A-Za-z.-]+))?(?:\+(?<meta>[0-9A-Za-z.-]+))?""")
    val major: Int
    val minor: Int
    val patch: Int
    val tag: String
    val meta: String

    init {
        val match = regex.findAll(versionString).first()
        major = match.groups["major"]!!.value.toInt()
        minor = match.groups["minor"]!!.value.toInt()
        patch = match.groups["patch"]!!.value.toInt()
        tag = match.groups["tag"]?.value ?: ""
        meta = match.groups["meta"]?.value ?: ""
    }

    fun isNewerThan(other: Version): Boolean {
        //TODO: testing + comparing other parts of the version string?
        if(major > other.major) return true
        if(minor > other.minor) return true
        if(patch > other.patch) return true
        return false
    }

    fun isDirty(): Boolean = meta.split(".").contains("dirty")

    override fun toString(): String {
        var result = "$major.$minor.$patch"
        if(tag.isNotEmpty()) result += "-$tag"
        if(meta.isNotEmpty()) result += "+$meta"
        return result
    }
}