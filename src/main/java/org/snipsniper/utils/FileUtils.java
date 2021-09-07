package org.snipsniper.utils;

import org.snipsniper.LogManager;

import java.io.File;

public class FileUtils {
    public static boolean delete(String file) {
        return delete(new File(file));
    }

    public static boolean delete(File file) {
        if(!file.delete()) {
            LogManager.log("File (%c) could not be deleted!", LogLevel.WARNING, file);
            return false;
        }
        return true;
    }

    public static boolean mkdir(String file) {
        return mkdir(new File(file));
    }

    public static boolean mkdir(File folder) {
        if(!folder.mkdir()) {
            LogManager.log("Folder (%c) could not be created.", LogLevel.WARNING, folder);
            return false;
        }
        return true;
    }

    public static boolean mkdirs(String file) {
        return mkdir(new File(file));
    }

    public static boolean mkdirs(File folder) {
        if(!folder.mkdirs()) {
            LogManager.log("Folders (%c) could not be created.", LogLevel.WARNING, folder);
            return false;
        }
        return true;
    }
}
