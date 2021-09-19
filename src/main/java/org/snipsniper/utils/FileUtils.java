package org.snipsniper.utils;

import org.snipsniper.LogManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileUtils {
    public static boolean delete(String file) {
        return delete(new File(file));
    }

    public static boolean delete(File file) {
        if(!file.exists()) return true;

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
        if(folder.exists()) return true;

        if(!folder.mkdir()) {
            LogManager.log("Folder (%c) could not be created.", LogLevel.WARNING, folder.getAbsolutePath());
            return false;
        }
        return true;
    }

    public static boolean mkdirs(String file) {
        return mkdirs(new File(file));
    }

    public static boolean mkdirs(File folder) {
        if(folder.exists()) return true;

        if(!folder.mkdirs()) {
            LogManager.log("Folders (%c) could not be created.", LogLevel.WARNING, folder);
            return false;
        }
        return true;
    }

    public static boolean mkdirs(String... folders) {
        File[] array = new File[folders.length];
        for(int i = 0; i < folders.length; i++)
            array[i] = new File(folders[i]);
        return mkdirs(array);
    }

    public static boolean mkdirs(File... folders) {
        boolean success = true;
        for(File folder : folders)
            if(!mkdirs(folder))
                success = false;
        return success;
    }

    public static File[] listFiles(String folder) {
        return listFiles(new File(folder));
    }

    public static File[] listFiles(File folder) {
        return folder.listFiles();
    }

    public static ArrayList<String> getFilesInFolders(String path) {
        ArrayList<String> result = new ArrayList<>();
        for(File file : FileUtils.listFiles(path)) {
            if(file.isDirectory())
                result.addAll(getFilesInFolders(file.getAbsolutePath()));
            if(!file.isDirectory())
                result.add(StringUtils.correctSlashes(file.getAbsolutePath()));
        }
        return result;
    }

    public static boolean exists(String... files) {
        File[] f = new File[files.length];
        for(int i = 0; i < files.length; i++)
            f[i] = new File(files[i]);
        return exists(f);
    }

    public static boolean exists(File... files) {
        boolean allExist = true;
        for(File file : files) {
            if(!file.exists())
                allExist = false;
        }
        return allExist;
    }

    public static void printFile(String filename, String text) {
        try {
            PrintWriter out = new PrintWriter(filename);
            out.print(text);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        return name.substring(lastIndexOf);
    }

    public static String getCanonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean copyFromJar(String jarPath, String path) {
        if(jarPath.startsWith("\\") || jarPath.startsWith("//"))
            LogManager.log("jarPath is starting with slashes, this generally does not work inside the jar!", LogLevel.WARNING);

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(jarPath);
        if(inputStream == null) return false;
        try {
            Files.copy(inputStream, new File(path).getCanonicalFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            LogManager.log("Issue copying from jar! Message: " + ex.getMessage(), LogLevel.ERROR);
            return false;
        }
        return true;
    }
}
