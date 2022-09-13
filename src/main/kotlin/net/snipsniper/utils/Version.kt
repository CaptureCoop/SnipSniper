package net.snipsniper.utils

import net.snipsniper.utils.enums.PlatformType
import net.snipsniper.utils.enums.ReleaseType
import org.capturecoop.ccutils.utils.CCStringUtils

class Version {
    private val maxDigits = 3
    private val digits: Array<Int>
    var releaseType = ReleaseType.UNKNOWN
        private set
    var platformType = PlatformType.UNKNOWN
        private set
    var buildDate = "UNKNOWN"
        private set
    var githash = "UNKNOWN"
        private set

    constructor(digits: String) {
        this.digits = digitsFromString(digits)
    }

    constructor(digits: String, releaseType: ReleaseType, platformType: PlatformType, buildDate: String, githash: String) {
        this.digits = digitsFromString(digits)
        this.releaseType = releaseType
        this.platformType = platformType
        this.buildDate = buildDate
        this.githash = githash
    }

    fun isNewerThan(other: Version): Boolean {
        if(equalsDigits(other)) return false

        val od = other.digits
        return if(digits[0] > od[0]) {
            true
        } else if(digits[0] == od[0]) {
            if(digits[1] > od[1]) true
            else if(digits[1] == od[1]) {
                digits[2] > od[2]
            } else false
        } else {
            false
        }
    }

    fun equalsDigits(other: Version): Boolean = digits.contentEquals(other.digits)

    private fun digitsFromString(string: String): Array<Int> {
        val parts = string.split(".")
        return Array(maxDigits) { parts[it].toInt() }
    }

    fun digitsToString(): String = "${digits[0]}.${digits[1]}.${digits[2]}"

    override fun toString(): String = "${digitsToString()}-${releaseType} rev-${githash}"
}