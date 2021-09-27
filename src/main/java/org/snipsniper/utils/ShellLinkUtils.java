package org.snipsniper.utils;

import com.erigir.mslinks.ShellLink;
import org.snipsniper.LogManager;
import org.snipsniper.utils.enums.LogLevel;

import java.io.IOException;

public class ShellLinkUtils {
    public static void createShellLink(String linkLocation, String originalLocation, String icon) {
        try {
            ShellLink sl = ShellLink.createLink(originalLocation);
            sl.setIconLocation(icon);
            sl.saveTo(linkLocation);
        } catch (IOException e) {
            LogManager.log("Issue creating shell link. linkLocation: %c, originalLocation: %c, icon: %c", LogLevel.ERROR, linkLocation, originalLocation, icon);
        }
    }
}
