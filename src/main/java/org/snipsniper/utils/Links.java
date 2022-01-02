package org.snipsniper.utils;

import org.snipsniper.LogManager;
import org.snipsniper.utils.enums.LogLevel;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Links {
    public static String KOFI = "https://ko-fi.com/SvenWollinger";
    public static String STABLE_VERSION_TXT = "https://raw.githubusercontent.com/SvenWollinger/SnipSniper/master/version.txt";
    public static String STABLE_JAR = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper.jar";
    public static String STABLE_INSTALLER = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper_Installer_Win.exe";
    public static String STABLE_PORTABLE = "https://github.com/CaptureCoop/SnipSniper/releases/latest/download/SnipSniper_Portable_Win.zip";
    public static String API_LATEST_COMMIT = "https://api.github.com/repos/capturecoop/SnipSniper/commits/master?per_page=1\n";
    public static String DEV_JAR = "https://SnipSniper.org/jars/SnipSniper.jar";

    public static URI getURI(String link) {
        try {
            return new URI(link);
        } catch (URISyntaxException exception) {
            LogManager.log("Could not get URI!", LogLevel.ERROR);
            LogManager.logStacktrace(exception, LogLevel.ERROR);
        }
        return null;
    }

    public static void openLink(String link) {
        try {
            Desktop.getDesktop().browse(getURI(link));
        } catch (IOException exception) {
            LogManager.log("Could open link!", LogLevel.ERROR);
            LogManager.logStacktrace(exception, LogLevel.ERROR);
        }
    }
}
