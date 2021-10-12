package org.snipsniper.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.time.LocalDate;
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

    public static String formatTimeArguments(String string) {
        String returnVal = string;
        LocalDateTime now = LocalDateTime.now();
        returnVal = returnVal.replaceAll("%hour%", getDateWithProperZero(now.getHour()));
        returnVal = returnVal.replaceAll("%minute%", getDateWithProperZero(now.getMinute()));
        returnVal = returnVal.replaceAll("%second%", getDateWithProperZero(now.getSecond()));
        return returnVal;
    }

    public static String getRandomString(int length, boolean useLetters, boolean useNumbers) {
        return RandomStringUtils.random(length, useLetters, useNumbers);
    }

    public static String formatDateArguments(String string) {
        String returnVal = string;
        LocalDate currentDate = LocalDate.now();
        returnVal = returnVal.replaceAll("%day%", getDateWithProperZero(currentDate.getDayOfMonth()));
        returnVal = returnVal.replaceAll("%month%", getDateWithProperZero(currentDate.getMonthValue()));
        returnVal = returnVal.replaceAll("%year%", String.valueOf(currentDate.getYear()));
        return returnVal;
    }

    public static String correctSlashes(String string) {
        return string.replaceAll("\\\\", "/").replaceAll("//", "/");
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
            newMessage = newMessage.replaceFirst("%c", replacer);
        }
        return newMessage;
    }

}
