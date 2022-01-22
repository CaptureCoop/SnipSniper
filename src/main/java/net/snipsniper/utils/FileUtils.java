package net.snipsniper.utils;

import org.capturecoop.cclogger.CCLogger;
import org.capturecoop.cclogger.LogLevel;
import org.capturecoop.ccutils.utils.CCStringUtils;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

public class FileUtils {

    public static boolean deleteRecursively(String folder) {
        return deleteRecursively(new File(folder));
    }

    public static boolean deleteRecursively(File folder) {
        if(!folder.isDirectory()) {
            FileUtils.delete(folder);
            return true;
        }

        for(File file : FileUtils.listFiles(folder)) {
            if(file.isDirectory()) {
                deleteRecursively(file);
            }
            FileUtils.delete(file);
        }
        FileUtils.delete(folder);
        return true;
    }

    public static boolean delete(String file) {
        return delete(new File(file));
    }

    public static boolean delete(File file) {
        if(!file.exists()) return true;

        if(!file.delete()) {
            CCLogger.log("File (%c) could not be deleted!", LogLevel.WARNING, file.getAbsolutePath());
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
            CCLogger.log("Folder (%c) could not be created.", LogLevel.WARNING, folder.getAbsolutePath());
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
            CCLogger.log("Folders (%c) could not be created.", LogLevel.WARNING, folder);
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
                result.add(CCStringUtils.correctSlashes(file.getAbsolutePath()));
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

    public static void openFolder(String path) {
        try {
            Desktop.getDesktop().open(new File(path));
        } catch (IOException ioException) {
            CCLogger.log("Could not open folder \"%c\"!", LogLevel.ERROR, path);
            CCLogger.logStacktrace(ioException, LogLevel.ERROR);
        }
    }

    public static void printFile(String filename, String text) {
        try {
            PrintWriter out = new PrintWriter(filename);
            out.print(text);
            out.close();
        } catch (FileNotFoundException fileNotFoundException) {
            CCLogger.log("Could not write to file \"%c\"!", LogLevel.ERROR, filename);
            CCLogger.logStacktrace(fileNotFoundException, LogLevel.ERROR);
        }
    }

    public static String getFileExtension(File file) {
        return getFileExtension(file, true);
    }

    public static String getFileExtension(File file, boolean dot) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return ""; // empty extension
        }
        if(!dot)
            lastIndexOf++;
        return name.substring(lastIndexOf);
    }

    public static String getCanonicalPath(String path) {
        try {
            return new File(path).getCanonicalPath();
        } catch (IOException ioException) {
            CCLogger.log("Could not get path for \"%c\"!", LogLevel.ERROR, path);
            CCLogger.logStacktrace(ioException, LogLevel.ERROR);
        }
        return null;
    }

    public static boolean copyFromJar(String jarPath, String path) {
        if(jarPath.startsWith("\\") || jarPath.startsWith("//"))
            CCLogger.log("jarPath is starting with slashes, this generally does not work inside the jar!", LogLevel.WARNING);

        if(FileUtils.exists(path))
            FileUtils.delete(path);

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(jarPath);
        if(inputStream == null) {
            CCLogger.log("InputStream is null! Copying failed! jarPath: %c, path: %c", LogLevel.ERROR, jarPath, path);
            return false;
        }
        try {
            Files.copy(inputStream, new File(path).getCanonicalFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            CCLogger.log("Issue copying from jar!", LogLevel.ERROR);
            CCLogger.logStacktrace(ioException, LogLevel.ERROR);
            return false;
        }
        return true;
    }

    public static String loadFileFromJar(String file) {
        StringBuilder content = new StringBuilder();
        try{
            String path = "net/snipsniper/resources/" + file;
            InputStream inputStream = ClassLoader.getSystemResourceAsStream(path);
            if(inputStream == null) {
                CCLogger.log(CCStringUtils.format("Could not load file %c from jar!", path), LogLevel.ERROR);
                return null;
            }
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(streamReader);

            for (String line; (line = in.readLine()) != null;)
                content.append(line);

            inputStream.close();
            streamReader.close();
        } catch (IOException ioException) {
            CCLogger.log("Could not load file: " + file, LogLevel.ERROR);
            CCLogger.logStacktrace(ioException, LogLevel.ERROR);
        }
        return content.toString();
    }
}
