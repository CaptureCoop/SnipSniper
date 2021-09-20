package org.snipsniper.utils;

import org.apache.commons.lang3.SystemUtils;

import java.time.LocalDate;

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

    public static String formatDateArguments(String string) {
        String returnVal = string;
        LocalDate currentDate = LocalDate.now();
        returnVal = returnVal.replaceAll("%day%", String.valueOf(currentDate.getDayOfMonth()));
        returnVal = returnVal.replaceAll("%month%", String.valueOf(currentDate.getMonthValue()));
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
        final int size = args.length;
        String newMessage = message;
        for(int i = 0; i < size; i++) {
            String replacer = "NULL";
            if(args[i] != null)
                replacer = args[i].toString();
            newMessage = newMessage.replaceFirst("%c", replacer);
        }
        return newMessage;
    }

}
