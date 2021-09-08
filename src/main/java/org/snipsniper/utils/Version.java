package org.snipsniper.utils;

import java.util.Arrays;

public class Version {
    private static final int MAX_DIGITS = 3;
    private final int[] digits;

    public Version(String version) {
        digits = new int[MAX_DIGITS];
        String[] parts = StringUtils.removeWhitespace(version).split("\\.");
        for(int i = 0; i < MAX_DIGITS; i++) {
            digits[i] = Integer.parseInt(parts[i]);
        }
    }

    public boolean isNewerThen(Version other) {
        int[] otherArr = other.digits;
        if(Arrays.equals(digits, otherArr)) return false;
        if(digits[0] > otherArr[0]) {
            return true;
        } else if(digits[0] == otherArr[0]) {
            if(digits[1] > otherArr[1]) return true;
            else if(digits[1] == otherArr[1]) {
                return digits[2] > otherArr[2];
            } else return false;
        } else {
            return false;
        }

    }

    public String toString() {
        return StringUtils.format("%c.%c.%c", digits[0], digits[1], digits[2]);
    }
}
