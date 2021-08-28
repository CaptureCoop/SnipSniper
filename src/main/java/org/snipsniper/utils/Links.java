package org.snipsniper.utils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Links {
    public static String KOFI = "https://ko-fi.com/SvenWollinger";

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