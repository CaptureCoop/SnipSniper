package org.snipsniper.utils;

public class MathUtils {
    public static boolean isInteger(String string) {
        try {
            Integer.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isDouble(String string) {
        try {
            Double.valueOf(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int clampInt(int number, int min, int max) {
        return Math.min(Math.max(number, min), max);
    }
}
