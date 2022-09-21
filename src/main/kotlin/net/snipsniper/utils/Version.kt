package net.snipsniper.utils

class Version(digits: String) {
    private val maxDigits = 3
    private val digits: Array<Int>

    init {
        this.digits = digitsFromString(digits)
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

    private fun equalsDigits(other: Version): Boolean = digits.contentEquals(other.digits)

    private fun digitsFromString(string: String): Array<Int> {
        val parts = string.split(".")
        return Array(maxDigits) { parts[it].toInt() }
    }

    fun digitsToString(): String = "${digits[0]}.${digits[1]}.${digits[2]}"
}