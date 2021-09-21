package org.snipsniper.utils;

import java.util.Arrays;

public class Version {
    private static final int MAX_DIGITS = 3;
    private final int[] digits;
    private ReleaseType releaseType = ReleaseType.UNKNOWN;
    private PlatformType platformType = PlatformType.UNKNOWN;
    private String buildDate = "UNKNOWN";
    private String githash = "UNKNOWN";

    public Version(String digits) {
        if(digits != null && !digits.isEmpty()) {
            this.digits = digitsFromString(digits);
        } else {
            this.digits = new int[3];
        }
    }

    public Version(String digits, ReleaseType releaseType, PlatformType platformType, String buildDate, String githash) {
        this.digits = digitsFromString(digits);
        this.releaseType = releaseType;
        this.platformType = platformType;
        this.buildDate = buildDate;
        this.githash = githash;
    }

    public boolean equals(Version other) {
        return (Arrays.equals(digits, other.digits));
    }

    public boolean isNewerThan(Version other) {
        if(equals(other)) return false;

        int[] otherArr = other.digits;
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

    public int[] digitsFromString(String string) {
        int[] ints = new int[MAX_DIGITS];
        String[] parts = StringUtils.removeWhitespace(string).split("\\.");
        for(int i = 0; i < MAX_DIGITS; i++) {
            ints[i] = Integer.parseInt(parts[i]);
        }
        return ints;
    }

    public String getDigits() {
        return StringUtils.format("%c.%c.%c", digits[0], digits[1], digits[2]);
    }

    public ReleaseType getReleaseType() {
        return releaseType;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public String getBuildDate() {
        return buildDate;
    }

    public String getGithash() {
        return githash;
    }
}
