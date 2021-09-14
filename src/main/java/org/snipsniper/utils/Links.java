package org.snipsniper.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Links {
    public static String KOFI = "https://ko-fi.com/SvenWollinger";
    public static String VERSION_TXT = "https://raw.githubusercontent.com/SvenWollinger/SnipSniper/master/version.txt";
    public static String STABLE_JAR = "https://github.com/CaptureCoop/SnipSniper/releases/download/3.7.0/SnipSniper.jar";

    public static URI getURI(String link) {
        try {
            return new URI(link);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void openLink(String link) {
        try {
            Desktop.getDesktop().browse(getURI(link));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
