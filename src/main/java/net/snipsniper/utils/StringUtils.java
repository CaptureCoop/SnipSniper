package net.snipsniper.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.time.LocalDateTime;

public class StringUtils {
    public static String removeWhitespace(String string) {
        return string.replaceAll("\\s+","");
    }

    public static String replaceVars(String string) {
        if(string.contains("%username%")) string = string.replace("%username%", System.getProperty("user.name"));
        if(SystemUtils.IS_OS_WINDOWS) if(string.contains("%userprofile%")) string = string.replace("%userprofile%", System.getenv("USERPROFILE"));
        if(SystemUtils.IS_OS_LINUX) if(string.contains("%userprofile%")) string = string.replace("%userprofile%", System.getProperty("user.home"));
        return string;
    }


    public static String getDateWithProperZero(int date) {
        String dateString = date + "";
        if(date < 10)
            dateString = "0" + date;
        return dateString;
    }

    public static String formatTimeArguments(String string, LocalDateTime time) {
        String returnVal = string;
        returnVal = returnVal.replaceAll("%hour%", getDateWithProperZero(time.getHour()));
        returnVal = returnVal.replaceAll("%minute%", getDateWithProperZero(time.getMinute()));
        returnVal = returnVal.replaceAll("%second%", getDateWithProperZero(time.getSecond()));
        returnVal = returnVal.replaceAll("%ms%", getDateWithProperZero(time.getNano() / 10000000));
        return returnVal;
    }

    public static String formatTimeArguments(String string) {
        return formatTimeArguments(string, LocalDateTime.now());
    }

    public static String getRandomString(int length, boolean useLetters, boolean useNumbers) {
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static String formatDateArguments(String string, LocalDateTime time) {
        String returnVal = string;
        returnVal = returnVal.replaceAll("%day%", getDateWithProperZero(time.getDayOfMonth()));
        returnVal = returnVal.replaceAll("%month%", getDateWithProperZero(time.getMonthValue()));
        returnVal = returnVal.replaceAll("%year%", String.valueOf(time.getYear()));
        return returnVal;
    }

    public static String formatDateArguments(String string) {
        return formatDateArguments(string, LocalDateTime.now());
    }

    public static String correctSlashes(String string) {
        String returnVal = string.replaceAll("\\\\", File.separator + File.separator).replaceAll("/", File.separator + File.separator);
        if(!returnVal.endsWith(File.separator))
            returnVal += File.separator;
        return returnVal;
    }

    public static boolean endsWith(String original, String... text) {
        for(String str : text)
            if(original.endsWith(str))
                return true;
        return false;
    }

    public static String format(final String message, final Object ...args) {
        String newMessage = message;
        for (Object arg : args) {
            String replacer = "NULL";
            if (arg != null)
                replacer = arg.toString();
            newMessage = org.apache.commons.lang3.StringUtils.replaceOnce(newMessage, "%c", replacer);
        }
        return newMessage;
    }

}
