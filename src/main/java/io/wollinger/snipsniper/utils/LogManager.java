package io.wollinger.snipsniper.utils;

import io.wollinger.snipsniper.SnipSniper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;

public class LogManager {

    public static void log(String id, String message, Level level) {
        String msg = "%DATETIME% [%PROFILE%] [%TYPE%]: %MESSAGE%";
        msg = msg.replace("%PROFILE%", id);
        msg = msg.replace("%TYPE%", level.toString());
        msg = msg.replace("%MESSAGE%", message);
        final LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss:SS");
        msg = msg.replace("%DATETIME%", "" + formatter.format(time));

        System.out.println(msg);

        if(!SnipSniper.isDemo) {
            if (SnipSniper.logFile == null) {
                LocalDateTime now = LocalDateTime.now();
                String filename = now.toString().replace(".", "_").replace(":", "_");
                filename += ".txt";

                SnipSniper.logFile = new File(SnipSniper.logFolder + filename);
                try {
                    if (SnipSniper.logFile.createNewFile())
                        LogManager.log(id, "Created new logfile at: " + SnipSniper.logFile.getAbsolutePath(), Level.INFO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            msg += "\n";

            try {
                Files.write(Paths.get(SnipSniper.logFile.getAbsolutePath()), msg.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void log(int profileID, String message, Level level) {
        LogManager.log("PRO" + profileID, message, level);
    }

}
