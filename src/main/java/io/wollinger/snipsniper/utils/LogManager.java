package io.wollinger.snipsniper.utils;

import static org.apache.commons.lang3.StringEscapeUtils.escapeHtml4;

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

    private static File logFile;
    public static String htmlLog = "";

    public static void log(String id, String message, Level level) {
        String msg = "%DATETIME% [%PROFILE%] [%TYPE%]: %MESSAGE%";
        msg = msg.replace("%PROFILE%", id);
        msg = msg.replace("%TYPE%", level.toString());
        msg = msg.replace("%MESSAGE%", message);
        final LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss:SS");
        msg = msg.replace("%DATETIME%", "" + formatter.format(time));

        System.out.println(msg);
        String color = "white";
        if(level == Level.WARNING)
            color = "yellow";
        else if(level == Level.SEVERE)
            color = "red";
        htmlLog += "<p style='margin-top:0'><font color='" + color + "'>" + escapeHtml4(msg) + "</font></p>";

        DebugConsole console = SnipSniper.getDebugConsole();
        if(console != null)
            console.update();

        if(!SnipSniper.isDemo()) {
            if (logFile == null) {
                LocalDateTime now = LocalDateTime.now();
                String filename = now.toString().replace(".", "_").replace(":", "_");
                filename += ".txt";

                logFile = new File(SnipSniper.getLogFolder() + filename);
                try {
                    if (logFile.createNewFile())
                        LogManager.log(id, "Created new logfile at: " + logFile.getAbsolutePath(), Level.INFO);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            msg += "\n";

            try {
                Files.write(Paths.get(logFile.getAbsolutePath()), msg.getBytes(), StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
