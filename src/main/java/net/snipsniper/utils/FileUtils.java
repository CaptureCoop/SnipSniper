package net.snipsniper.utils;

import org.capturecoop.cclogger.CCLogger;
import org.capturecoop.cclogger.CCLogLevel;
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
            return FileUtils.delete(folder);
        }

        boolean success = true;
        for(File file : FileUtils.listFiles(folder)) {
            if(file.isDirectory()) {
                if(!deleteRecursively(file))
                    success = false;
            }
            if(!FileUtils.delete(file))
                success = false;
        }
        if(!FileUtils.delete(folder))
            success = false;
        return success;
    }

    public static boolean delete(String file) {
        return delete(new File(file));
    }

    public static boolean delete(File file) {
        if(!file.exists()) return true;

        if(!file.delete()) {
            CCLogger.Companion.warn("File (" + file.getAbsolutePath() + ") could not be deleted!");
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
            CCLogger.Companion.warn("Folder (" + folder.getAbsolutePath() + ") could not be created.");
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
            CCLogger.Companion.warn("Folders (" + folder + ") could not be created.");
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
            CCLogger.Companion.error("Could not open folder \"" + path + "\"!");
            CCLogger.Companion.logStacktrace(ioException, CCLogLevel.ERROR);
        }
    }

    public static void printFile(String filename, String text) {
        try {
            PrintWriter out = new PrintWriter(filename);
            out.print(text);
            out.close();
        } catch (FileNotFoundException fileNotFoundException) {
            CCLogger.Companion.error("Could not write to file \"" + filename + "\"!");
            CCLogger.Companion.logStacktrace(fileNotFoundException, CCLogLevel.ERROR);
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
            CCLogger.Companion.error("Could not get path for \"" + path + "\"!");
            CCLogger.Companion.logStacktrace(ioException, CCLogLevel.ERROR);
        }
        return null;
    }

    public static boolean copyFromJar(String jarPath, String path) {
        if(jarPath.startsWith("\\") || jarPath.startsWith("//"))
            CCLogger.Companion.warn("jarPath is starting with slashes, this generally does not work inside the jar!");

        if(FileUtils.exists(path))
            FileUtils.delete(path);

        InputStream inputStream = ClassLoader.getSystemResourceAsStream(jarPath);
        if(inputStream == null) {
            CCLogger.Companion.error(String.format("InputStream is null! Copying failed! jarPath: %s, path: %s", jarPath, path));
            return false;
        }
        try {
            Files.copy(inputStream, new File(path).getCanonicalFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ioException) {
            CCLogger.Companion.log("Issue copying from jar!", CCLogLevel.ERROR);
            CCLogger.Companion.logStacktrace(ioException, CCLogLevel.ERROR);
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
                CCLogger.Companion.log(CCStringUtils.format("Could not load file %c from jar!", path), CCLogLevel.ERROR);
                return null;
            }
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader in = new BufferedReader(streamReader);

            for (String line; (line = in.readLine()) != null;)
                content.append(line);

            inputStream.close();
            streamReader.close();
        } catch (IOException ioException) {
            CCLogger.Companion.log("Could not load file: " + file, CCLogLevel.ERROR);
            CCLogger.Companion.logStacktrace(ioException, CCLogLevel.ERROR);
        }
        return content.toString();
    }
}
